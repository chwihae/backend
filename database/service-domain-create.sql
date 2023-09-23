-- Tables for domain
create table users
(
    created_at  datetime default current_timestamp() not null comment '생성 시간',
    deleted_at  datetime                             null comment '삭제 시간',
    id_users    bigint auto_increment primary key,
    modified_at datetime default current_timestamp() not null on update current_timestamp() comment '수정 시간',
    email       varchar(50)                          not null comment '이메일',
    constraint uk_users_email unique (email)
);

create table question
(
    close_at      datetime                                  not null comment '질문 종료 시간',
    created_at    datetime default current_timestamp()      not null comment '생성 시간',
    deleted_at    datetime                                  null comment '삭제 시간',
    id_question   bigint auto_increment
        primary key,
    id_questioner bigint                                    not null comment '사용자 PK',
    modified_at   datetime default current_timestamp()      not null on update current_timestamp() comment '수정 시간',
    content       text                                      null comment '질문 내용',
    status        enum ('COMPLETED', 'IN_PROGRESS')         not null,
    title         varchar(50)                               not null comment '질문 제목',
    type          enum ('CAREER', 'COMPANY', 'ETC', 'SPEC') not null,
    constraint fk_question_users foreign key (id_questioner) references users (id_users)
);

create index idx_question_type on question (type);
create index idx_question_status on question (status);
create index idx_question_type_status on question (type, status);
create index idx_question_close_at on question (close_at);
create index idx_question_deleted_at on question (deleted_at);
create index idx_question_close_at_deleted_at on question (close_at, deleted_at);
create index idx_question_status_close_at on question (status, close_at);

create table bookmark
(
    created_at  datetime default current_timestamp() not null comment '생성 시간',
    deleted_at  datetime                             null comment '삭제 시간',
    id_bookmark bigint auto_increment
        primary key,
    id_question bigint                               not null comment '질문 PK',
    id_users    bigint                               not null comment '사용자 PK',
    modified_at datetime default current_timestamp() not null on update current_timestamp() comment '수정 시간',
    constraint fk_bookmark_question foreign key (id_question) references question (id_question),
    constraint fk_bookmark_users foreign key (id_users) references users (id_users)
);

create  index idx_bookmark_question on bookmark (id_question);

create  index idx_bookmark_question_user on bookmark (id_question, id_users);

create  index idx_bookmark_user on bookmark (id_users);

create table commenter_alias
(
    created_at         datetime default current_timestamp() not null comment '생성 시간',
    deleted_at         datetime                             null comment '삭제 시간',
    id_commenter       bigint                               not null comment '사용자 PK',
    id_commenter_alias bigint auto_increment
        primary key,
    id_question        bigint                               not null comment '질문 PK',
    modified_at        datetime default current_timestamp() not null on update current_timestamp() comment '수정 시간',
    alias              varchar(50)                          not null comment '댓글 작성자 별칭',
    constraint uk_commenter_alias_question_commeter unique (id_question, id_commenter),
    constraint fk_commenter_alias_question foreign key (id_question) references question (id_question),
    constraint fk_commenter_alias_users foreign key (id_commenter) references users (id_users)
);

create table comment
(
    created_at         datetime default current_timestamp() not null comment '생성 시간',
    deleted_at         datetime                             null comment '삭제 시간',
    id_comment         bigint auto_increment
        primary key,
    id_commenter       bigint                               not null comment '사용자 PK',
    id_commenter_alias bigint                               not null comment '댓글 작성자 별칭 PK',
    id_question        bigint                               not null comment '질문 PK',
    modified_at        datetime default current_timestamp() not null on update current_timestamp() comment '수정 시간',
    content            varchar(1000)                        not null comment '댓글 내용',
    constraint fk_comment_commenter_alias foreign key (id_commenter_alias) references commenter_alias (id_commenter_alias),
    constraint fk_comment_question foreign key (id_question) references question (id_question),
    constraint fk_comment_users foreign key (id_commenter) references users (id_users)
);

create  index idx_comment_commenter on comment (id_commenter);

create  index idx_comment_commenter_question on comment (id_question, id_commenter);

create  index idx_comment_question on comment (id_question);

create table commenter_sequence
(
    sequence         int                                  not null comment '댓글 작성자 부여 번호',
    created_at       datetime default current_timestamp() not null comment '생성 시간',
    deleted_at       datetime                             null comment '삭제 시간',
    id_commenter_seq bigint auto_increment primary key,
    id_question      bigint                               not null comment '질문 PK',
    modified_at      datetime default current_timestamp() not null on update current_timestamp() comment '수정 시간',
    constraint uk_commenter_seq_question unique (id_question),
    constraint fk_commenter_sequence_question foreign key (id_question) references question (id_question)
);

create index idx_commenter_seq_question on commenter_sequence (id_question);

create table `option`
(
    created_at  datetime default current_timestamp() not null comment '생성 시간',
    deleted_at  datetime                             null comment '삭제 시간',
    id_option   bigint auto_increment
        primary key,
    id_question bigint                               not null comment '질문 PK',
    modified_at datetime default current_timestamp() not null on update current_timestamp() comment '수정 시간',
    name        varchar(100)                         not null comment '옵션 내용',
    constraint fk_option_question foreign key (id_question) references question (id_question)
);

create  index idx_option_question on `option` (id_question);

create  table question_view
(
    view_count  int                                  not null comment '질문 조회 수',
    created_at  datetime default current_timestamp() not null comment '생성 시간',
    deleted_at  datetime                             null comment '삭제 시간',
    id_question bigint                               not null comment '질문 PK',
    id_view     bigint auto_increment primary key,
    modified_at datetime default current_timestamp() not null on update current_timestamp() comment '수정 시간',
    constraint uk_question_view_question unique (id_question),
    constraint fk_question_view_question foreign key (id_question) references question (id_question)
);

create  table vote
(
    valid       bit      default b'1'                null comment '투표 유니크 검증을 위한 필드',
    created_at  datetime default current_timestamp() not null comment '생성 시간',
    deleted_at  datetime                             null comment '삭제 시간',
    id_option   bigint                               not null comment '옵션 PK',
    id_question bigint                               not null comment '질문 PK',
    id_vote     bigint auto_increment primary key,
    id_voter    bigint                               not null comment '사용자 PK',
    modified_at datetime default current_timestamp() not null on update current_timestamp() comment '수정 시간',
    constraint uk_vote_user_question_valid unique (id_voter, id_question, valid),
    constraint fk_vote_option foreign key (id_option) references `option` (id_option),
    constraint fk_vote_question foreign key (id_question) references question (id_question),
    constraint fk_vote_users foreign key (id_voter) references users (id_users)
);

create  index idx_vote_question on vote (id_question);

create  index idx_vote_question_users on vote (id_question, id_voter);

create  index idx_vote_users on vote (id_voter);


