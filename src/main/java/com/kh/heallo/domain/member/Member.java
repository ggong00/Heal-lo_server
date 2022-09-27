package com.kh.heallo.domain.member;

import com.kh.heallo.domain.review.Review;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {
  private Long memno;          //number(8)
  private String memid;        //varchar2(40)
  private String mempw;       //varchar2(20)
  private String memtel;       //varchar2(13)
  private String memnickname;  //varchar2(30)
  private String mememail;     //varchar2(30)
  private String memname;      //varchar2(12)
  private String memcode;      //varchar2(15)
  private LocalDateTime memcdate;       //date
  private LocalDateTime memudate;       //date
  private Review review;
}
