package com.chwihae.infra;

import com.chwihae.client.kakao.KakaoTokenFeignClient;
import com.chwihae.client.kakao.KakaoUserInfoFeignClient;
import com.chwihae.config.properties.JwtTokenProperties;
import com.chwihae.config.security.JwtTokenHandler;
import com.chwihae.domain.option.OptionRepository;
import com.chwihae.domain.question.QuestionRepository;
import com.chwihae.domain.user.UserRepository;
import com.chwihae.domain.vote.VoteRepository;
import com.chwihae.service.question.QuestionService;
import com.chwihae.service.user.UserService;
import com.chwihae.service.vote.VoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;

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
}
