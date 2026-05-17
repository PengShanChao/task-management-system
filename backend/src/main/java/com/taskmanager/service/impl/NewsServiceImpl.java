package com.taskmanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.taskmanager.dto.NewsQuery;
import com.taskmanager.dto.NewsResponse;
import com.taskmanager.entity.News;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.mapper.NewsMapper;
import com.taskmanager.mapper.TaskMapper;
import com.taskmanager.mapper.TaskNewsRelationMapper;
import com.taskmanager.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private static final List<String> RSS_FEEDS = List.of(
            "https://www.oschina.net/news/rss",
            "https://www.infoq.cn/feed",
            "https://36kr.com/feed"
    );

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "the", "in", "on", "at", "to", "for", "of", "is", "are", "was", "were",
            "it", "and", "or", "not", "with", "this", "that", "from", "be", "has", "have",
            "的", "是", "在", "了", "和", "与", "或", "不", "这", "那", "从", "到",
            "an", "but", "by", "can", "do", "if", "my", "no", "so", "up", "we", "he", "she",
            "也", "就", "都", "而", "及", "去", "要", "会", "能", "为", "被", "把"
    );

    private final NewsMapper newsMapper;
    private final TaskMapper taskMapper;
    private final TaskNewsRelationMapper taskNewsRelationMapper;

    @Override
    @Scheduled(fixedRate = 1800000)
    public int refreshNews() {
        int totalNew = 0;
        for (String feedUrl : RSS_FEEDS) {
            try {
                URL url = new URL(feedUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; TaskManager/1.0)");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(conn.getInputStream()));
                String source = feed.getTitle() != null ? feed.getTitle() : feedUrl;
                int count = 0;
                for (SyndEntry entry : feed.getEntries()) {
                    String link = entry.getLink();
                    if (link == null || newsMapper.selectByUrl(link) != null) {
                        continue;
                    }
                    News news = new News();
                    news.setTitle(entry.getTitle());
                    news.setUrl(link);
                    news.setSource(source);
                    if (entry.getDescription() != null) {
                        String summary = entry.getDescription().getValue();
                        news.setSummary(summary.length() > 1000 ? summary.substring(0, 1000) : summary);
                    }
                    if (entry.getPublishedDate() != null) {
                        news.setPublishedAt(LocalDateTime.ofInstant(
                                entry.getPublishedDate().toInstant(), ZoneId.systemDefault()));
                    } else {
                        news.setPublishedAt(LocalDateTime.now());
                    }
                    newsMapper.insert(news);
                    count++;
                }
                totalNew += count;
                log.info("RSS feed {} fetched: {} new items", feedUrl, count);
            } catch (Exception e) {
                log.error("Failed to fetch RSS feed: {}", feedUrl, e);
            }
        }
        return totalNew;
    }

    @Override
    public IPage<NewsResponse> listNews(NewsQuery query) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<News>()
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(News::getTitle, query.getKeyword())
                        .or()
                        .like(News::getSummary, query.getKeyword()))
                .eq(query.getSource() != null, News::getSource, query.getSource())
                .orderByDesc(News::getPublishedAt);

        Page<News> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<News> newsPage = newsMapper.selectPage(page, wrapper);

        Set<Long> linkedNewsIds = getLinkedNewsIdsForCurrentUser();

        return newsPage.convert(n -> {
            NewsResponse response = NewsResponse.builder()
                    .id(n.getId())
                    .title(n.getTitle())
                    .summary(n.getSummary())
                    .url(n.getUrl())
                    .source(n.getSource())
                    .publishedAt(n.getPublishedAt())
                    .createdAt(n.getCreatedAt())
                    .build();
            if (!linkedNewsIds.isEmpty()) {
                response.setRelatedToCurrentUserTasks(linkedNewsIds.contains(n.getId()));
            }
            return response;
        });
    }

    @Override
    public List<NewsResponse> getNewsByTaskId(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            return Collections.emptyList();
        }
        checkTaskAccess(task);

        // Get manually linked news
        List<Long> linkedIds = taskNewsRelationMapper.selectNewsIdsByTaskId(taskId);
        List<News> linkedNews = linkedIds.isEmpty() ? Collections.emptyList()
                : newsMapper.selectBatchIds(linkedIds);

        // Keyword-based auto-suggest
        Set<String> keywords = extractKeywords(task.getTitle());
        Set<News> suggestedNews = new LinkedHashSet<>();
        for (String kw : keywords) {
            if (suggestedNews.size() >= 15) break;
            List<News> results = newsMapper.selectByKeyword(kw, 5);
            suggestedNews.addAll(results);
        }

        // Remove already-linked from suggestions
        Set<Long> linkedIdSet = new HashSet<>(linkedIds);
        suggestedNews.removeIf(n -> linkedIdSet.contains(n.getId()));

        List<NewsResponse> result = new ArrayList<>();
        // Linked news first
        for (News n : linkedNews) {
            result.add(toResponse(n, true));
        }
        // Suggested news
        for (News n : suggestedNews) {
            if (result.size() >= 20) break;
            result.add(toResponse(n, false));
        }
        return result;
    }

    @Override
    public void linkTaskToNews(Long taskId, Long newsId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found");
        }
        checkTaskAccess(task);
        if (taskNewsRelationMapper.existsRelation(taskId, newsId) > 0) {
            return; // already linked
        }
        com.taskmanager.entity.TaskNewsRelation relation = new com.taskmanager.entity.TaskNewsRelation();
        relation.setTaskId(taskId);
        relation.setNewsId(newsId);
        taskNewsRelationMapper.insert(relation);
    }

    @Override
    public void unlinkTaskFromNews(Long taskId, Long newsId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found");
        }
        checkTaskAccess(task);
        taskNewsRelationMapper.deleteRelation(taskId, newsId);
    }

    @Override
    public List<NewsResponse> getRelatedToUser() {
        User currentUser = getCurrentUser();
        List<Task> userTasks = taskMapper.selectList(
                new LambdaQueryWrapper<Task>().eq(Task::getUserId, currentUser.getId()));
        if (userTasks.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> allKeywords = new LinkedHashSet<>();
        for (Task task : userTasks) {
            allKeywords.addAll(extractKeywords(task.getTitle()));
        }
        if (allKeywords.isEmpty()) {
            return Collections.emptyList();
        }

        Set<News> results = new LinkedHashSet<>();
        for (String kw : allKeywords) {
            if (results.size() >= 5) break;
            results.addAll(newsMapper.selectByKeyword(kw, 2));
        }

        return results.stream()
                .limit(5)
                .map(n -> toResponse(n, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getSources() {
        return newsMapper.selectDistinctSources();
    }

    private Set<String> extractKeywords(String title) {
        Set<String> keywords = new LinkedHashSet<>();
        if (!StringUtils.hasText(title)) return keywords;

        String[] words = title.split("[\\s,，、。/；;：:！!？?()（）\\[\\]【】\"'\"'\\-+=|\\\\]+");
        for (String word : words) {
            String trimmed = word.trim().toLowerCase();
            if (trimmed.length() >= 2 && !STOP_WORDS.contains(trimmed)) {
                keywords.add(trimmed);
            }
        }
        return keywords;
    }

    private Set<Long> getLinkedNewsIdsForCurrentUser() {
        try {
            User user = getCurrentUser();
            List<Task> userTasks = taskMapper.selectList(
                    new LambdaQueryWrapper<Task>().eq(Task::getUserId, user.getId()));
            if (userTasks.isEmpty()) return Collections.emptySet();
            List<Long> taskIds = userTasks.stream().map(Task::getId).collect(Collectors.toList());
            return new HashSet<>(taskNewsRelationMapper.selectNewsIdsByUserTaskIds(taskIds));
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    private void checkTaskAccess(Task task) {
        User currentUser = getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole()) && !task.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private NewsResponse toResponse(News n, boolean linked) {
        return NewsResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .summary(n.getSummary())
                .url(n.getUrl())
                .source(n.getSource())
                .publishedAt(n.getPublishedAt())
                .createdAt(n.getCreatedAt())
                .relatedToCurrentUserTasks(linked)
                .build();
    }
}
