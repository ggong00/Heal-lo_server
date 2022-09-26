package com.kh.heallo.web.member.controller;

import com.kh.heallo.domain.member.Member;
import com.kh.heallo.domain.member.svc.MemberSVC;
import com.kh.heallo.domain.review.Review;
import com.kh.heallo.web.member.dto.*;
import com.kh.heallo.web.member.session.LoginMember;
import com.kh.heallo.web.review.dto.ReviewDto;
import com.kh.heallo.web.session.Session;
import com.kh.heallo.web.utility.DtoModifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

  private final MemberSVC memberSVC;

  private final DtoModifier dtoModifier;

  //회원가입
  @GetMapping("/join")
  public String joinForm(Model model){
    model.addAttribute("form", new JoinForm());
    return "member/join";
  }

  //회원가입 처리
  @PostMapping("/join")
  public String join(@Valid @ModelAttribute("form")
                     JoinForm joinForm,
                     BindingResult bindingResult){
    log.info("joinForm={}",joinForm);

    if(bindingResult.hasErrors()){
      log.info("errors={}",bindingResult);
      return "member/join";
    }

    if(joinForm.getMemid().toLowerCase().trim().length() <= 5 &&
            joinForm.getMemid().toUpperCase().trim().length() >= 15){
      bindingResult.rejectValue("memid","chk.memid.length", "아이디 규칙을 지켜주세요");
      return "member/join";
    }

    Member member = new Member();
    member.setMemid(joinForm.getMemid());
    member.setMempw(joinForm.getMempw());
    member.setMemtel(joinForm.getMemtel());
    member.setMemnickname(joinForm.getMemnickname());
    member.setMememail(joinForm.getMememail());
    member.setMemname(joinForm.getMemname());

    memberSVC.join(member);

    return "redirect:/members/login";
  }

  //로그인 화면
  @GetMapping("/login")
  public String loginForm(@ModelAttribute("form") LoginForm loginForm){

    return "login/login";
  }

  //로그인 처리
  @PostMapping("/login")
  public String login(@Valid @ModelAttribute("form")LoginForm loginForm,
                      BindingResult bindingResult,
                      HttpServletRequest request,
                      @RequestParam(value = "requestURI",required = false,defaultValue = "/") String requestURI
  ){

    //기본 검증
    if (bindingResult.hasErrors()){
      log.info("bindingResult={}",bindingResult);
      return "login/login";
    }

    log.info("member, {} " , loginForm);

    //회원유무
    Optional<Member> member = memberSVC.login(loginForm.getMemid(), loginForm.getMempw());

    if(member.isEmpty()){
      bindingResult.reject("LoginForm.login","회원정보가 없습니다");
      return "login/login";
    }

    //회원인경우
    Member findedMember = member.get();

    //세션에 회원정보 저장
    LoginMember loginMember = new LoginMember(findedMember.getMemno(), findedMember.getMemnickname());

    //request.getSession(false) : 세션정보가 있으면 가져오고 없으면 세션을 만듦
    HttpSession session = request.getSession(true);
    session.setAttribute(Session.LOGIN_MEMBER.name(), loginMember);

    if (requestURI.equals("/members/login")) {
      return "redirect:/";
    }
    return "redirect:"+requestURI;
  }

  //로그아웃
  @GetMapping("logout")
  public String logout(HttpServletRequest request){
    //request.getSession(false) : 세션정보가 있으면 가져오고 없으면 세션을 만들지 않음
    HttpSession session = request.getSession(false);
    if(session != null){
      session.invalidate();
    }
    return "redirect:/";  //초기화면으로 이동
  }

  //조회와 동시에 수정
  @GetMapping("/{id}/edit")
  public String findById(@PathVariable("id") Long memno, Model model,HttpServletRequest request){

    //회원번호 조회
    HttpSession session = request.getSession(false);
    if(session != null && session.getAttribute(Session.LOGIN_MEMBER.name()) != null) {
      LoginMember loginMember = (LoginMember) session.getAttribute(Session.LOGIN_MEMBER.name());
      memno = loginMember.getMemno();
    }

    Member findedMember = memberSVC.findById(memno);

    EditForm editForm = new EditForm();
    editForm.setMemno(memno);
    editForm.setMemid(findedMember.getMemid());
    editForm.setMempw(findedMember.getMempw());
    editForm.setMemtel(findedMember.getMemtel());
    editForm.setMemnickname(findedMember.getMemnickname());
    editForm.setMememail(findedMember.getMememail());
    editForm.setMemname(findedMember.getMemname());

    log.info("memno={}",memno);
    log.info("editForm={}",editForm);

    model.addAttribute("form",editForm);

    return "member/my_page";
  }

  //수정처리
  @PostMapping("/{id}/edit")
  public String update(@PathVariable("id") Long memno, EditForm editForm, HttpServletRequest request){

    //회원번호 조회
    HttpSession session = request.getSession(false);
    if(session != null && session.getAttribute(Session.LOGIN_MEMBER.name()) != null) {
      LoginMember loginMember = (LoginMember) session.getAttribute(Session.LOGIN_MEMBER.name());
      memno = loginMember.getMemno();
    }

    Member member = new Member();
    member.setMemno(memno);
    member.setMemname(editForm.getMemname());
    member.setMemnickname(editForm.getMemnickname());
    member.setMememail(editForm.getMememail());
    member.setMempw(editForm.getMempw());
    member.setMemtel(editForm.getMemtel());
    member.setMemudate(editForm.getMemudate());

    memberSVC.update(memno,member);

    log.info("editForm={}",editForm);
    log.info("member={}",member);

    return "redirect:/members/"+memno+"/edit";
  }

  //삭제(탈퇴)
  @GetMapping("/{id}/del")
  public String delete(@PathVariable("id") String memid) {

    memberSVC.del(memid);

    log.info("memid={}",memid);
    return "redirect:/";
  }

  //아이디 찾기 화면
  @GetMapping("/find_id")
  public String findIdPWForm(){

    return "find_id_pw/find_id";
  }

  //아이디 찾기 처리
  @PostMapping("/find_id")
  public String findIdPW(@ModelAttribute("form") FindIdForm findIdForm, Model model){

    FindIdForm findId = new FindIdForm();
    findId.setMemname(findIdForm.getMemname());
    findId.setMememail(findIdForm.getMememail());

    Member id = memberSVC.findId(findId.getMemname(), findId.getMememail());
    findId.setMemid(id.getMemid());

    log.info("findId={}", findId);
    model.addAttribute("form", findId);
    return "find_id_pw/success_find_id";
  }

  //비밀번호
  @GetMapping("/find_pw")
  public String findIdPWForm2(){

    return "find_id_pw/find_pw";
  }

  @PostMapping("/find_pw")
  public String findPw(@ModelAttribute("form")FindPwForm findPwForm, Model model){

    FindPwForm findPw = new FindPwForm();
    findPw.setMemid(findPwForm.getMemid());
    findPw.setMemname(findPwForm.getMemname());
    findPw.setMememail(findPwForm.getMememail());

    Member pw = memberSVC.findPw(findPw.getMemid(), findPw.getMemname(), findPw.getMememail());
    findPw.setMempw(pw.getMempw());

    log.info("findPw={}", findPw);
    model.addAttribute("form", findPw);
    return "find_id_pw/success_find_pw";
  }

  //마이페이지 활동 (게시글) 활동 이동 시 첫 페이지
  @GetMapping("/{id}/board")
  public String myActivityBoard(@PathVariable("id")Long memno){

    return "member/my_page_activity_board";
  }

  //마이페이지 활동 (댓글)
  @GetMapping("/{id}/reply")
  public String myActivityReply(){

    return "member/my_page_activity_reply";
  }

  //마이페이지 활동 (리뷰)
  @GetMapping("/{id}/review")
  public String myActivityReview(@PathVariable("id")Long memno, Long rvno,Model model){

    List<Review> reviews = memberSVC.findReviewByMemno(memno,rvno);
    List<Review> list = new ArrayList<>();

    log.info("reviews={}",reviews);

    List<ReviewDto> reviewDtoList = reviews.stream().map(review -> {
      ReviewDto reviewDto = new ReviewDto();
      BeanUtils.copyProperties(review, reviewDto);

      return reviewDto;
    }).collect(Collectors.toList());


    reviews.stream().forEach(review->{
      log.info("review={}",review);
      Review review1 = new Review();
      BeanUtils.copyProperties(review,review1);
      list.add(review1);
    });

    log.info("list={}",reviewDtoList);

    model.addAttribute("list", reviewDtoList);
    return "member/my_page_activity_review";
  }

}
