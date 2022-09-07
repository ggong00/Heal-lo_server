package com.kh.heallo.domain.facility;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Criteria {
    private String fcaddr;      //지역
    private String fctype;      //운동시설분류
    private String fcname;       //운동시설명
    private Integer pageNo;         //페이지번호
    private Integer numOfRow;       //한 페이지 수
}
