package com.kh.heallo.web.calendar.controller;

import com.kh.heallo.domain.calendar.Calendar;
import com.kh.heallo.domain.calendar.svc.CalendarSVC;
import com.kh.heallo.domain.uploadfile.AttachCode;
import com.kh.heallo.domain.uploadfile.FileData;
import com.kh.heallo.domain.uploadfile.svc.UploadFileSVC;
import com.kh.heallo.web.calendar.dto.AddForm;
import com.kh.heallo.web.calendar.dto.DayForm;
import com.kh.heallo.web.calendar.dto.EditForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Binding;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/calendar")
public class CalendarController {

  private final CalendarSVC calendarSVC;
  private final UploadFileSVC uploadFileSVC;

  // 달력 메인 (전체)
  @GetMapping
  public String calendar(
//    HttpServletRequest request
  ) {

//    // 로그인 여부
//    HttpSession session = request.getSession(false);
//    if (session != null) {
//      session.invalidate();
//    }

    return "calendar/calendarForm";
  }

  // 운동기록 등록화면
  @GetMapping("/{rdate}/add")
  public String addForm(@PathVariable String rdate, Model model) {
    model.addAttribute("rdate", rdate);
    return "calendar/addForm";
  }

  // 운동기록 등록처리
  @PostMapping("/{rdate}/add")
  public String add(
    @ModelAttribute("form") AddForm addForm,
    BindingResult bindingResult
//    List<MultipartFile> imageFiles
  ) {
    // 기본 검증
    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
      return "calendar/addForm";
    }

    // 오브젝트 검증
    // 첨부파일 & 내용 아무것도 없을 때


    Calendar calendarRecord = new Calendar();
    String rdate = addForm.getCdRDate();
    calendarRecord.setCdRDate(rdate);
    calendarRecord.setCdContent(addForm.getCdContent());

    // 첨부파일 있으면 함께 등록
    if (!addForm.getImageFiles().get(0).isEmpty()) {
      calendarSVC.save(rdate, calendarRecord, addForm.getImageFiles());
    } else {
      calendarSVC.save(rdate, calendarRecord);
    }

    return "redirect:/calendar/"+rdate;
  }

  // 운동기록 조회
  @GetMapping("/{rdate}")
  public String findByDate(
      @PathVariable String rdate,
      Model model
  ) {
    DayForm dayForm = new DayForm();
    Optional<Calendar> foundRecord = calendarSVC.findByDate(rdate);
    if (!foundRecord.isEmpty()) {
      dayForm.setCdContent(foundRecord.get().getCdContent());
      dayForm.setCdUDate(foundRecord.get().getCdUDate());
      dayForm.setCdCDate(foundRecord.get().getCdCDate());
      String cdRDate = foundRecord.get().getCdRDate().substring(0, 10);
      dayForm.setCdRDate(cdRDate);
    }

    // 첨부파일 있으면 조회
    List<FileData> foundImageList = uploadFileSVC.findImages(AttachCode.CD_CODE, foundRecord.get().getCdno());
    if (foundImageList.size() > 0) {
      List<FileData> imageFiles = new ArrayList<>();
      for (FileData file : foundImageList) {
        imageFiles.add(file);
      }
      dayForm.setFoundImageFiles(imageFiles);
    }

    model.addAttribute("form", dayForm);
    return "calendar/dayForm";
  }

  // 운동기록 수정화면
  @GetMapping("/{rdate}/edit")
  public String editForm(
      @PathVariable String rdate,
      Model model
  ) {
    EditForm editForm = new EditForm();
    Optional<Calendar> foundRecord = calendarSVC.findByDate(rdate);
    if (!foundRecord.isEmpty()) {
      BeanUtils.copyProperties(foundRecord.get(), editForm);
    }

    // 첨부파일 있으면 조회
    List<FileData> foundImageList = uploadFileSVC.findImages(AttachCode.CD_CODE, foundRecord.get().getCdno());
    if (foundImageList.size() > 0) {
      List<FileData> imageFiles = new ArrayList<>();
      for (FileData file : foundImageList) {
        imageFiles.add(file);
      }
      editForm.setFoundImageFiles(imageFiles);
    }


    model.addAttribute("form", editForm);
    return "calendar/editForm";
  }

  // 운동기록 수정처리
  @PostMapping("/{rdate}/edit")
  public String edit(
      @PathVariable String rdate,
      @ModelAttribute("form") EditForm editForm,
      Model model
//      List<MultipartFile> imageFiles
  ) {

    Calendar calendarRecord = new Calendar();
    calendarRecord.setCdContent(editForm.getCdContent());

    if (!editForm.getImageFiles().get(0).isEmpty()) {
      calendarSVC.update(rdate, calendarRecord, editForm.getImageFiles());
    } else {
      calendarSVC.update(rdate, calendarRecord);
    }

    model.addAttribute("form", editForm);
    return "redirect:/calendar/"+rdate;
  }

  // 운동기록 삭제
  @GetMapping("/{rdate}/del")
  public String del(@PathVariable String rdate) {
    calendarSVC.del(rdate);

    return "redirect:/calendar";
  }
}
