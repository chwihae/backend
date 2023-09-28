package com.chwihae.infra.test;

import com.chwihae.batch.closequestion.CloseQuestionJobConfig;
import com.chwihae.client.kakao.KakaoTokenFeignClient;
import com.chwihae.client.kakao.KakaoUserInfoFeignClient;
import com.chwihae.config.properties.JwtTokenProperties;
import com.chwihae.config.redis.QuestionViewCacheRepository;
import com.chwihae.config.redis.UserContextCacheRepository;
import com.chwihae.config.security.JwtTokenHandler;
import com.chwihae.domain.bookmark.BookmarkRepository;
import com.chwihae.domain.comment.CommentRepository;
import com.chwihae.domain.commenter.CommenterAliasRepository;
import com.chwihae.domain.commenter.CommenterSequenceRepository;
import com.chwihae.domain.option.OptionRepository;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.domain.question.QuestionViewRepository;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.domain.vote.VoteRepository;
import com.chwihae.dto.user.UserContext;
import com.chwihae.event.question.QuestionViewEventListener;
import com.chwihae.infra.support.IntegrationTestSupport;
import com.chwihae.service.bookmark.BookmarkService;
import com.chwihae.service.comment.CommentService;
import com.chwihae.service.commenter.CommenterSequenceService;
import com.chwihae.service.question.QuestionService;
import com.chwihae.service.question.core.QuestionViewService;
import com.chwihae.service.question.strategy.UserQuestionsFilterStrategyProvider;
import com.chwihae.service.user.UserService;
import com.chwihae.service.vote.VoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

@IntegrationTestSupport
public class AbstractIntegrationTest extends AbstractContainerBaseTest {

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected KakaoUserInfoFeignClient kakaoUserInfoFeignClient;

    @Autowired
    protected KakaoTokenFeignClient kakaoTokenFeignClient;

    @Autowired
    protected JwtTokenProperties jwtTokenProperties;

    @Autowired
    protected QuestionService questionService;

    @Autowired
    protected JwtTokenHandler jwtTokenHandler;

    @Autowired
    protected UserService userService;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected VoteService voteService;

    @Autowired
    protected VoteRepository voteRepository;

    @Autowired
    protected QuestionRepository questionRepository;

    @Autowired
    protected OptionRepository optionRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected CommenterSequenceService commenterSequenceService;

    @Autowired
    protected CommenterSequenceRepository commenterSequenceRepository;

    @Autowired
    protected CommentRepository commentRepository;

    @Autowired
    protected CommentService commentService;

    @Autowired
    protected CommenterAliasRepository commenterAliasRepository;

    @Autowired
    protected BookmarkRepository bookmarkRepository;

    @Autowired
    protected BookmarkService bookmarkService;

    @MockBean
    protected QuestionViewEventListener questionViewEventListener;

    @Autowired
    protected QuestionViewService questionViewService;

    @Autowired
    protected QuestionViewRepository questionViewRepository;

    @Autowired
    protected QuestionViewCacheRepository questionViewCacheRepository;

    @Autowired
    protected UserContextCacheRepository userContextCacheRepository;

    @Autowired
    protected RedisTemplate<String, UserContext> userContextRedisTemplate;

    @Autowired
    protected RedisTemplate<String, Long> questionViewRedisTemplate;

    @Autowired
    protected CloseQuestionJobConfig closeQuestionJobConfig;

    @Autowired
    protected UserQuestionsFilterStrategyProvider userQuestionsFilterStrategyProvider;
}
