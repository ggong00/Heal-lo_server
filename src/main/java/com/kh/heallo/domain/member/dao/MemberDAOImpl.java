package com.kh.heallo.domain.member.dao;

import com.kh.heallo.domain.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberDAOImpl implements  MemberDAO{

  private final JdbcTemplate jdbcTemplate;

  /**
   * 가입
   *
   * @param member 가입정보
   * @return 가입건수
   */
  @Override
  public Long join(Member member) {
    StringBuffer sql = new StringBuffer();
    sql.append(" insert into member ");
    sql.append(" values (member_memno_seq.nextval ,? ,? ,? ,? ,? ,? ,'normal' ,sysdate ,sysdate ) ");

    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(con -> {
      PreparedStatement preparedStatement = con.prepareStatement(sql.toString(), new String[]{"memno"});
      preparedStatement.setString(1, member.getMemid());
      preparedStatement.setString(2, member.getMempw());
      preparedStatement.setString(3, member.getMemtel());
      preparedStatement.setString(4, member.getMemnickname());
      preparedStatement.setString(5, member.getMememail());
      preparedStatement.setString(6, member.getMemname());

      return preparedStatement;
    }, keyHolder);

    Long memno = Long.valueOf(keyHolder.getKeys().get("memno").toString());

    member.setMemno(memno);
    return memno;
  }

  /**
   * 조회 BY 회원번호
   * @param memno 회원번호
   * @return  회원정보
   */
  @Override
  public Member findById(Long memno) {
    StringBuffer sql = new StringBuffer();

    sql.append("select * ");
    sql.append("  from member ");
    sql.append(" where memno= ? ");

    Member findedMember = null;
    try{
      findedMember = jdbcTemplate.queryForObject(sql.toString(), new BeanPropertyRowMapper<>(Member.class), memno);
    }catch (DataAccessException e) {
      log.info("찾고자하는 회원이 없습니다={}", memno);
    }
    return findedMember;
  }

  /**
   * 수정
   * @param memid  아이디
   * @param member 수정할 정보
   * @return  수정건수
   */
  @Override
  public void update(String memid, Member member) {
    StringBuffer sql = new StringBuffer();

    sql.append("update member ");
    sql.append("   set mempw = ?, ");
    sql.append("       memtel = ?, ");
    sql.append("       memnickname = ?, ");
    sql.append("       mememail = ?, ");
    sql.append("       memname = ?, ");
    sql.append("       memudate = sysdate ");
    sql.append(" where memid = ? ");

    jdbcTemplate.update(sql.toString(),member.getMempw(), member.getMemtel(),
            member.getMemnickname(), member.getMememail(), member.getMemname(),memid);
  }

  /**
   * 탈퇴
   * @param memid 비밀번호
   * @return
   */
  @Override
  public void del(String memid) {
    String sql = "delete MEMBER where memid= ? ";

    jdbcTemplate.update(sql, memid);
  }

  /**
   * 로그인
   * @param memid 아이디
   * @param mempw 비밀번호
   * @return  회원
   */
  @Override
  public Optional<Member> login(String memid, String mempw) {
    StringBuffer sql = new StringBuffer();
    sql.append(" select * ");
    sql.append("  from member ");
    sql.append(" where memid = ? ");
    sql.append("   and mempw = ? ");

    try {
      Member member = jdbcTemplate.queryForObject(
              sql.toString(),
              new BeanPropertyRowMapper<>(Member.class),
              memid,mempw
      );

      return Optional.of(member);
    } catch (DataAccessException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }
}
