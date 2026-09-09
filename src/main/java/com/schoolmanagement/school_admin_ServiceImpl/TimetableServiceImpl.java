package com.schoolmanagement.school_admin_ServiceImpl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.schoolmanagement.school_admin_DTO.DivisionTimetableResponseDTO;
import com.schoolmanagement.school_admin_DTO.BulkCreateResponseDTO;
import com.schoolmanagement.school_admin_DTO.SchoolTimetableResponseDTO;
import com.schoolmanagement.school_admin_DTO.TeacherTimetableResponseDTO;
import com.schoolmanagement.school_admin_DTO.TeacherTimetableResponseDTO.*;
import com.schoolmanagement.school_admin_DTO.TeacherAppTimetableResponseDTO;
import com.schoolmanagement.school_admin_DTO.StudentAppTimetableResponseDTO;
import com.schoolmanagement.school_admin_DTO.TimetableDTO;
import com.schoolmanagement.school_admin_Entity.*;
import com.schoolmanagement.school_admin_Repo.*;
import com.schoolmanagement.school_admin_Service.TimetableService;

@Service
public class TimetableServiceImpl implements TimetableService {

    private final TimetableRepository timetableRepository;
    private final SchoolRepository schoolRepository;
    private final AddStandardRepository standardRepository;
    private final AddDivisionRepository divisionRepository;
    private final AddSubjectRepository subjectRepository;
    private final AddTeacherRepository teacherRepository;
    private final AcademicYearRepository academicYearRepository;
    private final NotificationRepository notificationRepository;
    private final AddStudentRepository addStudentRepository;

    public TimetableServiceImpl(TimetableRepository timetableRepository, SchoolRepository schoolRepository,
            AddStandardRepository standardRepository, AddDivisionRepository divisionRepository,
            AddSubjectRepository subjectRepository, AddTeacherRepository teacherRepository,
            AcademicYearRepository academicYearRepository,
            NotificationRepository notificationRepository,
            AddStudentRepository addStudentRepository) {
        this.timetableRepository = timetableRepository;
        this.schoolRepository = schoolRepository;
        this.standardRepository = standardRepository;
        this.divisionRepository = divisionRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.academicYearRepository = academicYearRepository;
        this.notificationRepository = notificationRepository;
        this.addStudentRepository = addStudentRepository;
    }

    // ==================== Helper: entity -> DTO ====================
    private TimetableDTO toDTO(Timetable tt) {
        TimetableDTO dto = new TimetableDTO();
        dto.setTimetableId(tt.getTimetableId());
        if (tt.getSchool() != null)
            dto.setSchoolId(tt.getSchool().getSchoolId());
        if (tt.getAcademicYear() != null)
            dto.setAcademicYearId(tt.getAcademicYear().getAcademicYearId());
        if (tt.getAddStandard() != null)
            dto.setStandardId(tt.getAddStandard().getStandardId());
        if (tt.getAddDivision() != null)
            dto.setDivisionId(tt.getAddDivision().getDivisionId());
        if (tt.getAddSubject() != null)
            dto.setSubjectId(tt.getAddSubject().getSubjectId());
        if (tt.getAddTeacher() != null)
            dto.setTeacherId(tt.getAddTeacher().getId());
        dto.setDayOfWeek(tt.getDayOfWeek());
        dto.setStartTime(tt.getStartTime());
        dto.setEndTime(tt.getEndTime());
        dto.setPeriodNumber(tt.getPeriodNumber());
        dto.setRoomNumber(tt.getRoomNumber());
        dto.setIsSubstitute(tt.getIsSubstitute());
        dto.setOriginalTeacher(tt.getOriginalTeacher());
        dto.setIsCancelled(tt.getIsCancelled());
        dto.setStatus(calculateStatus(tt.getStartTime(), tt.getEndTime(), LocalDate.now()));
        return dto;
    }

    // ==================== Helper: extract day name from date string
    // ====================
    private String extractDayOfWeek(String date) {
        try {
            return LocalDate.parse(date).getDayOfWeek().name();
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format. Use yyyy-MM-dd. e.g. 2026-03-21");
        }
    }

    // ==================== Helper: map to ClassPeriodEntry ====================


    private String calculateStatus(LocalTime startTime, LocalTime endTime, LocalDate date) {
        if (startTime == null || endTime == null)
            return "upcoming";
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        if (date.isBefore(today))
            return "completed";
        if (date.isAfter(today))
            return "upcoming";
        if (now.isBefore(startTime))
            return "upcoming";
        if (now.isAfter(endTime))
            return "completed";
        return "ongoing";
    }

    // ==================== Helper: conflict checker ====================
    private void checkForConflicts(TimetableDTO dto, Long currentTimetableId) {
        Long schoolId = dto.getSchoolId();
        Long academicYearId = dto.getAcademicYearId();
        Long standardId = dto.getStandardId();
        Long divisionId = dto.getDivisionId();
        Long teacherId = dto.getTeacherId();
        String dayOfWeek = dto.getDayOfWeek();
        LocalTime startTime = dto.getStartTime();
        LocalTime endTime = dto.getEndTime();
        String roomNumber = dto.getRoomNumber();

        // If this is an update, load missing fields from the existing database entry
        if (currentTimetableId != null) {
            Timetable existing = timetableRepository.findById(currentTimetableId).orElse(null);
            if (existing != null) {
                if (schoolId == null) schoolId = existing.getSchool().getSchoolId();
                if (academicYearId == null && existing.getAcademicYear() != null) academicYearId = existing.getAcademicYear().getAcademicYearId();
                if (standardId == null) standardId = existing.getAddStandard().getStandardId();
                if (divisionId == null) divisionId = existing.getAddDivision().getDivisionId();
                if (teacherId == null && existing.getAddTeacher() != null) teacherId = existing.getAddTeacher().getId();
                if (dayOfWeek == null) dayOfWeek = existing.getDayOfWeek();
                if (startTime == null) startTime = existing.getStartTime();
                if (endTime == null) endTime = existing.getEndTime();
                if (roomNumber == null) roomNumber = existing.getRoomNumber();
            }
        }

        if (schoolId == null || academicYearId == null || dayOfWeek == null || startTime == null || endTime == null) {
            return;
        }

        // 1. Division (Class) Time Overlap Check
        if (standardId != null && divisionId != null) {
            List<Timetable> divisionSlots = timetableRepository
                .findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndAcademicYear_AcademicYearId(
                    schoolId, standardId, divisionId, academicYearId);
            for (Timetable slot : divisionSlots) {
                if (currentTimetableId != null && slot.getTimetableId().equals(currentTimetableId)) {
                    continue;
                }
                if (slot.getDayOfWeek().equalsIgnoreCase(dayOfWeek)) {
                    LocalTime s1 = slot.getStartTime();
                    LocalTime e1 = slot.getEndTime();
                    if (startTime.isBefore(e1) && endTime.isAfter(s1)) {
                        throw new RuntimeException("This division section already has a lecture (" + 
                            (slot.getAddSubject() != null ? slot.getAddSubject().getSubjectName() : "Scheduled") + 
                            ") scheduled on " + dayOfWeek + " between " + s1 + " and " + e1);
                    }
                }
            }
        }

        // 2. Teacher Double Booking Check
        if (teacherId != null) {
            List<Timetable> teacherSlots = timetableRepository
                .findBySchool_SchoolIdAndAddTeacher_IdAndAcademicYear_AcademicYearId(
                    schoolId, teacherId, academicYearId);
            for (Timetable slot : teacherSlots) {
                if (currentTimetableId != null && slot.getTimetableId().equals(currentTimetableId)) {
                    continue;
                }
                if (slot.getDayOfWeek().equalsIgnoreCase(dayOfWeek)) {
                    LocalTime s1 = slot.getStartTime();
                    LocalTime e1 = slot.getEndTime();
                    if (startTime.isBefore(e1) && endTime.isAfter(s1)) {
                        String tName = slot.getAddTeacher() != null ? slot.getAddTeacher().getTeacherName() : "the teacher";
                        throw new RuntimeException(tName + " is already scheduled on " + dayOfWeek + 
                            " between " + s1 + " and " + e1 + " in Class " + 
                            (slot.getAddStandard() != null ? slot.getAddStandard().getStandard() : "") + 
                            " Section " + (slot.getAddDivision() != null ? slot.getAddDivision().getDivisionName() : ""));
                    }
                }
            }
        }

        // 3. Room Clash Check
        if (roomNumber != null && !roomNumber.isBlank()) {
            List<Timetable> schoolSlots = timetableRepository
                .findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId);
            for (Timetable slot : schoolSlots) {
                if (currentTimetableId != null && slot.getTimetableId().equals(currentTimetableId)) {
                    continue;
                }
                if (slot.getRoomNumber() != null && slot.getRoomNumber().equalsIgnoreCase(roomNumber)
                    && slot.getDayOfWeek().equalsIgnoreCase(dayOfWeek)) {
                    LocalTime s1 = slot.getStartTime();
                    LocalTime e1 = slot.getEndTime();
                    if (startTime.isBefore(e1) && endTime.isAfter(s1)) {
                        throw new RuntimeException("Room " + roomNumber + " is already occupied on " + dayOfWeek + 
                            " between " + s1 + " and " + e1 + " by Class " + 
                            (slot.getAddStandard() != null ? slot.getAddStandard().getStandard() : "") + 
                            " Section " + (slot.getAddDivision() != null ? slot.getAddDivision().getDivisionName() : ""));
                    }
                }
            }
        }
    }

    // ==================== CREATE ====================
    @Override
    public TimetableDTO create(TimetableDTO dto) {
        checkForConflicts(dto, null);

        Timetable tt = new Timetable();
        tt.setSchool(schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found")));
        tt.setAddStandard(standardRepository.findByStandardId(dto.getStandardId())
                .orElseThrow(() -> new RuntimeException("Standard not found")));
        tt.setAddDivision(divisionRepository.findByDivisionId(dto.getDivisionId())
                .orElseThrow(() -> new RuntimeException("Division not found")));
        tt.setAddSubject(subjectRepository.findBySubjectId(dto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found")));
        if (dto.getTeacherId() != null) {
            tt.setAddTeacher(teacherRepository.findById(dto.getTeacherId()).orElse(null));
        }
        if (dto.getAcademicYearId() != null) {
            tt.setAcademicYear(academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new RuntimeException("Academic Year not found")));
        }
        tt.setDayOfWeek(dto.getDayOfWeek());
        tt.setStartTime(dto.getStartTime());
        tt.setEndTime(dto.getEndTime());
        tt.setPeriodNumber(dto.getPeriodNumber());
        tt.setRoomNumber(dto.getRoomNumber());
        tt.setIsSubstitute(dto.getIsSubstitute() != null ? dto.getIsSubstitute() : false);
        tt.setOriginalTeacher(dto.getOriginalTeacher());
        tt.setIsCancelled(dto.getIsCancelled() != null ? dto.getIsCancelled() : false);
        // status is computed dynamically, not persisted
        return toDTO(timetableRepository.save(tt));
    }

    // ==================== CREATE BULK ====================
    @Override
    public BulkCreateResponseDTO createBulk(List<TimetableDTO> entries) {
        List<BulkCreateResponseDTO.FailedEntry> failed = new ArrayList<>();
        int saved = 0;

        for (int i = 0; i < entries.size(); i++) {
            TimetableDTO dto = entries.get(i);
            try {
                if (dto.getSchoolId() == null)
                    throw new RuntimeException("schoolId is required");
                if (dto.getStandardId() == null)
                    throw new RuntimeException("standardId is required");
                if (dto.getDivisionId() == null)
                    throw new RuntimeException("divisionId is required");
                if (dto.getSubjectId() == null)
                    throw new RuntimeException("subjectId is required");
                if (dto.getDayOfWeek() == null || dto.getDayOfWeek().isBlank())
                    throw new RuntimeException("dayOfWeek is required");
                if (dto.getStartTime() == null)
                    throw new RuntimeException("startTime is required");
                if (dto.getEndTime() == null)
                    throw new RuntimeException("endTime is required");

                if (!schoolRepository.existsById(dto.getSchoolId()))
                    throw new RuntimeException("School not found: " + dto.getSchoolId());
                if (!standardRepository.existsById(dto.getStandardId()))
                    throw new RuntimeException("Standard not found: " + dto.getStandardId());
                if (!divisionRepository.existsById(dto.getDivisionId()))
                    throw new RuntimeException("Division not found: " + dto.getDivisionId());
                if (!subjectRepository.existsById(dto.getSubjectId()))
                    throw new RuntimeException("Subject not found: " + dto.getSubjectId());
                if (dto.getTeacherId() != null && !teacherRepository.existsById(dto.getTeacherId()))
                    throw new RuntimeException("Teacher not found: " + dto.getTeacherId());
                if (dto.getAcademicYearId() != null && !academicYearRepository.existsById(dto.getAcademicYearId()))
                    throw new RuntimeException("Academic Year not found: " + dto.getAcademicYearId());

                create(dto);
                saved++;
            } catch (Exception e) {
                failed.add(new BulkCreateResponseDTO.FailedEntry(i, e.getMessage()));
            }
        }

        BulkCreateResponseDTO response = new BulkCreateResponseDTO();
        response.setTotalReceived(entries.size());
        response.setTotalSaved(saved);
        response.setTotalFailed(failed.size());
        response.setFailed(failed);
        return response;
    }

    // ==================== GET BY DIVISION ====================
    @Override
    public List<TimetableDTO> getByDivision(Long schoolId, Long standardId, Long divisionId, Long academicYearId) {
        return timetableRepository
                .findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndAcademicYear_AcademicYearId(
                        schoolId, standardId, divisionId, academicYearId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ==================== GET BY DIVISION STRUCTURED ====================
    @Override
    public DivisionTimetableResponseDTO getByDivisionStructured(Long schoolId, Long standardId, Long divisionId,
            Long academicYearId) {
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        List<Timetable> entries = timetableRepository
                .findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndAcademicYear_AcademicYearId(
                        schoolId, standardId, divisionId, academicYearId);

        List<DivisionTimetableResponseDTO.PeriodEntry> periods = entries.stream()
                .sorted(Comparator.comparing((Timetable tt) -> tt.getDayOfWeek() != null ? tt.getDayOfWeek() : "")
                        .thenComparing(tt -> tt.getStartTime(), Comparator.nullsLast(Comparator.naturalOrder())))
                .map(tt -> {
                    DivisionTimetableResponseDTO.PeriodEntry pe = new DivisionTimetableResponseDTO.PeriodEntry();
                    pe.setPeriodId(tt.getTimetableId());
                    pe.setPeriodNumber(tt.getPeriodNumber());
                    pe.setDayOfWeek(tt.getDayOfWeek());
                    pe.setStartTime(tt.getStartTime() != null ? tt.getStartTime().format(timeFmt) : null);
                    pe.setEndTime(tt.getEndTime() != null ? tt.getEndTime().format(timeFmt) : null);
                    pe.setRoomNumber(tt.getRoomNumber());
                    if (tt.getAddSubject() != null)
                        pe.setSubject(new DivisionTimetableResponseDTO.SubjectInfo(
                                tt.getAddSubject().getSubjectId(), tt.getAddSubject().getSubjectName()));
                    if (tt.getAddTeacher() != null)
                        pe.setTeacher(new DivisionTimetableResponseDTO.TeacherInfo(
                                tt.getAddTeacher().getId(), tt.getAddTeacher().getTeacherName()));
                    pe.setIsSubstitute(tt.getIsSubstitute() != null ? tt.getIsSubstitute() : false);
                    pe.setOriginalTeacher(tt.getOriginalTeacher() != null ? tt.getOriginalTeacher() : "");
                    pe.setIsCancelled(tt.getIsCancelled() != null ? tt.getIsCancelled() : false);
                    pe.setStatus(calculateStatus(tt.getStartTime(), tt.getEndTime(), LocalDate.now()));
                    return pe;
                }).collect(Collectors.toList());

        DivisionTimetableResponseDTO response = new DivisionTimetableResponseDTO();
        response.setSchoolId(schoolId);
        response.setAcademicYearId(academicYearId);
        response.setStandardId(standardId);
        response.setDivisionId(divisionId);
        response.setTimetable(periods);
        return response;
    }

    // ==================== GET BY TEACHER ====================
    @Override
    public List<TimetableDTO> getByTeacher(Long schoolId, Long teacherId, Long academicYearId) {
        return timetableRepository
                .findBySchool_SchoolIdAndAddTeacher_IdAndAcademicYear_AcademicYearId(schoolId, teacherId,
                        academicYearId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ==================== GET BY TEACHER DASHBOARD ====================
    @Override
    public TeacherTimetableResponseDTO getTeacherDashboard(Long schoolId, Long teacherId, Long academicYearId,
            String date) {
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate requestedDate = (date != null && !date.isBlank()) ? LocalDate.parse(date) : LocalDate.now();
        LocalTime now = LocalTime.now();
        String todayName = requestedDate.getDayOfWeek().name();

        AddTeacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        List<Timetable> allEntries = timetableRepository
                .findBySchool_SchoolIdAndAddTeacher_IdAndAcademicYear_AcademicYearId(schoolId, teacherId,
                        academicYearId);

        Map<Long, SubjectInfo> subjectMap = new LinkedHashMap<>();
        Map<String, ClassDivision> classDivisionMap = new LinkedHashMap<>();
        for (Timetable tt : allEntries) {
            if (tt.getAddSubject() != null) {
                Long sid = tt.getAddSubject().getSubjectId();
                if (!subjectMap.containsKey(sid)) {
                    SubjectInfo si = new SubjectInfo();
                    si.setId(sid);
                    si.setName(tt.getAddSubject().getSubjectName());
                    subjectMap.put(sid, si);
                }
            }
            if (tt.getAddStandard() != null && tt.getAddDivision() != null) {
                String key = tt.getAddStandard().getStandardId() + "_" + tt.getAddDivision().getDivisionId();
                if (!classDivisionMap.containsKey(key)) {
                    ClassDivision cd = new ClassDivision();
                    cd.setClassName(tt.getAddStandard().getStandard());
                    cd.setDivision(tt.getAddDivision().getDivisionName());
                    classDivisionMap.put(key, cd);
                }
            }
        }

        TeacherInfo teacherInfo = new TeacherInfo();
        teacherInfo.setId(teacher.getId());
        teacherInfo.setName(teacher.getTeacherName());
        teacherInfo.setProfileImage(teacher.getProfilePhoto() != null ? teacher.getProfilePhoto() : "");
        teacherInfo.setSubjects(new ArrayList<>(subjectMap.values()));
        teacherInfo.setClassDivisions(new ArrayList<>(classDivisionMap.values()));

        List<ScheduleEntry> todaySchedule = allEntries.stream()
                .filter(tt -> todayName.equalsIgnoreCase(tt.getDayOfWeek()))
                .sorted(Comparator.comparing((Timetable tt) -> tt.getStartTime(),
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(tt -> {
                    ScheduleEntry se = new ScheduleEntry();
                    se.setPeriodId(tt.getTimetableId());
                    se.setStartTime(tt.getStartTime() != null ? tt.getStartTime().format(timeFmt) : "");
                    se.setEndTime(tt.getEndTime() != null ? tt.getEndTime().format(timeFmt) : "");
                    se.setClassName(tt.getAddStandard() != null ? tt.getAddStandard().getStandard() : "");
                    se.setDivision(tt.getAddDivision() != null ? tt.getAddDivision().getDivisionName() : "");
                    se.setSubject(tt.getAddSubject() != null ? tt.getAddSubject().getSubjectName() : "");
                    se.setRoom(tt.getRoomNumber() != null ? tt.getRoomNumber() : "");
                    se.setStatus(calculateStatus(tt.getStartTime(), tt.getEndTime(), requestedDate));

                    se.setIsSubstitute(tt.getIsSubstitute() != null ? tt.getIsSubstitute() : false);
                    se.setOriginalTeacher(tt.getOriginalTeacher() != null ? tt.getOriginalTeacher() : "");
                    se.setIsCancelled(tt.getIsCancelled() != null ? tt.getIsCancelled() : false);
                    return se;
                }).collect(Collectors.toList());

        ScheduleEntry nextLecture = todaySchedule.stream()
                .filter(se -> "upcoming".equals(se.getStatus())).findFirst().orElse(null);
        if (nextLecture == null) {
            DayOfWeek today = requestedDate.getDayOfWeek();
            nextLecture = allEntries.stream()
                    .filter(tt -> {
                        try {
                            return DayOfWeek.valueOf(tt.getDayOfWeek().toUpperCase()).getValue() > today.getValue();
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .sorted(Comparator.comparing((Timetable tt) -> {
                        try {
                            return DayOfWeek.valueOf(tt.getDayOfWeek().toUpperCase()).getValue();
                        } catch (Exception e) {
                            return 7;
                        }
                    })
                            .thenComparing(tt -> tt.getStartTime(), Comparator.nullsLast(Comparator.naturalOrder())))
                    .findFirst().map(tt -> {
                        ScheduleEntry se = new ScheduleEntry();
                        se.setPeriodId(tt.getTimetableId());
                        se.setStartTime(tt.getStartTime() != null ? tt.getStartTime().format(timeFmt) : "");
                        se.setEndTime(tt.getEndTime() != null ? tt.getEndTime().format(timeFmt) : "");
                        se.setClassName(tt.getAddStandard() != null ? tt.getAddStandard().getStandard() : "");
                        se.setDivision(tt.getAddDivision() != null ? tt.getAddDivision().getDivisionName() : "");
                        se.setSubject(tt.getAddSubject() != null ? tt.getAddSubject().getSubjectName() : "");
                        se.setRoom(tt.getRoomNumber() != null ? tt.getRoomNumber() : "");
                        return se;
                    }).orElse(null);
        }

        List<AnnouncementEntry> announcements = notificationRepository
                .findBySchool_SchoolIdAndIsActiveTrue(schoolId).stream()
                .filter(n -> {
                    String a = n.getTargetAudience();
                    return a == null || "ALL".equalsIgnoreCase(a) || "TEACHERS".equalsIgnoreCase(a);
                })
                .map(n -> {
                    AnnouncementEntry ae = new AnnouncementEntry();
                    ae.setId(n.getNotificationId());
                    ae.setTitle(n.getTitle());
                    ae.setDate(n.getSentAt() != null ? n.getSentAt().toLocalDate().toString() : "");
                    return ae;
                })
                .collect(Collectors.toList());

        TeacherTimetableResponseDTO response = new TeacherTimetableResponseDTO();
        response.setTeacher(teacherInfo);
        response.setTodaySchedule(todaySchedule);
        response.setNextLecture(nextLecture);
        response.setAnnouncements(announcements);
        return response;
    }

    // ==================== GET BY CLASS ====================
    @Override
    public Object getByClass(Long schoolId, Long teacherId, Long standardId, Long divisionId, Long academicYearId, String date, String type) {
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate requestedDate = (date != null && !date.isBlank()) ? LocalDate.parse(date) : LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalDate todayDate = LocalDate.now();

        TeacherAppTimetableResponseDTO.PeriodConfig config = new TeacherAppTimetableResponseDTO.PeriodConfig();
        config.setTotalPeriods(8);
        config.setBreakAfterPeriod(4);

        if ("week".equalsIgnoreCase(type)) {
            DayOfWeek currentDay = requestedDate.getDayOfWeek();
            LocalDate startOfWeek = requestedDate.minusDays(currentDay.getValue() - 1);
            LocalDate endOfWeek = startOfWeek.plusDays(6);

            List<Timetable> allEntries = timetableRepository
                    .findBySchool_SchoolIdAndAddTeacher_IdAndAcademicYear_AcademicYearId(schoolId, teacherId,
                            academicYearId);

            List<TeacherAppTimetableResponseDTO.DaySchedule> days = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                LocalDate dDate = startOfWeek.plusDays(i);
                String dayName = dDate.getDayOfWeek().name();

                List<TeacherAppTimetableResponseDTO.ScheduleEntry> daySched = allEntries.stream()
                        .filter(tt -> dayName.equalsIgnoreCase(tt.getDayOfWeek()))
                        .filter(tt -> tt.getAddStandard() != null && tt.getAddStandard().getStandardId().equals(standardId))
                        .filter(tt -> tt.getAddDivision() != null && tt.getAddDivision().getDivisionId().equals(divisionId))
                        .sorted(Comparator.comparing((Timetable tt) -> tt.getStartTime(),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(tt -> mapToAppScheduleEntry(tt, timeFmt, dDate, todayDate, now, false))
                        .collect(Collectors.toList());

                TeacherAppTimetableResponseDTO.DaySchedule ds = new TeacherAppTimetableResponseDTO.DaySchedule();
                ds.setDate(dDate.toString());
                ds.setDayName(dayName.substring(0, 1) + dayName.substring(1).toLowerCase());
                ds.setDayStatus(daySched.isEmpty() ? "holiday" : "normal");
                ds.setSchedule(daySched);
                days.add(ds);
            }

            TeacherAppTimetableResponseDTO.WeekViewData weekData = new TeacherAppTimetableResponseDTO.WeekViewData();
            weekData.setType("week");
            weekData.setView("self");
            weekData.setStartDate(startOfWeek.toString());
            weekData.setEndDate(endOfWeek.toString());
            weekData.setPeriodConfig(config);
            weekData.setDays(days);
            return weekData;

        } else {
            String dayName = requestedDate.getDayOfWeek().name();
            List<Timetable> allEntries = timetableRepository
                    .findBySchool_SchoolIdAndAddTeacher_IdAndAcademicYear_AcademicYearId(schoolId, teacherId,
                            academicYearId);

            List<TeacherAppTimetableResponseDTO.ScheduleEntry> daySched = allEntries.stream()
                    .filter(tt -> dayName.equalsIgnoreCase(tt.getDayOfWeek()))
                    .filter(tt -> tt.getAddStandard() != null && tt.getAddStandard().getStandardId().equals(standardId))
                    .filter(tt -> tt.getAddDivision() != null && tt.getAddDivision().getDivisionId().equals(divisionId))
                    .sorted(Comparator.comparing((Timetable tt) -> tt.getStartTime(),
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(tt -> mapToAppScheduleEntry(tt, timeFmt, requestedDate, todayDate, now, false))
                    .collect(Collectors.toList());

            TeacherAppTimetableResponseDTO.DayViewData dayData = new TeacherAppTimetableResponseDTO.DayViewData();
            dayData.setType("day");
            dayData.setView("self");
            dayData.setDate(requestedDate.toString());
            dayData.setIsToday(requestedDate.equals(todayDate));
            dayData.setServerTime(now.format(timeFmt));
            dayData.setDayStatus(daySched.isEmpty() ? "holiday" : "normal");
            dayData.setPeriodConfig(config);
            dayData.setSchedule(daySched);
            return dayData;
        }
    }

    // ==================== GET BY SCHOOL ====================
    @Override
    public SchoolTimetableResponseDTO getBySchool(Long schoolId, Long academicYearId, String date) {
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        String dayOfWeek = extractDayOfWeek(date);
        LocalDate requestedDate = (date != null && !date.isBlank()) ? LocalDate.parse(date) : LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalDate todayDate = LocalDate.now();

        List<SchoolTimetableResponseDTO.SchoolPeriodEntry> periods = timetableRepository
                .findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId).stream()
                .filter(tt -> dayOfWeek.equalsIgnoreCase(tt.getDayOfWeek()))
                .sorted(Comparator
                        .comparing(
                                (Timetable tt) -> tt.getAddStandard() != null ? tt.getAddStandard().getStandard() : "")
                        .thenComparing(tt -> tt.getAddDivision() != null ? tt.getAddDivision().getDivisionName() : "")
                        .thenComparing(tt -> tt.getStartTime(), Comparator.nullsLast(Comparator.naturalOrder())))
                .map(tt -> {
                    SchoolTimetableResponseDTO.SchoolPeriodEntry pe = new SchoolTimetableResponseDTO.SchoolPeriodEntry();
                    pe.setPeriodId(tt.getTimetableId());
                    pe.setPeriodNumber(tt.getPeriodNumber());
                    pe.setStartTime(tt.getStartTime() != null ? tt.getStartTime().format(timeFmt) : null);
                    pe.setEndTime(tt.getEndTime() != null ? tt.getEndTime().format(timeFmt) : null);
                    pe.setRoomNumber(tt.getRoomNumber());
                    if (tt.getAddStandard() != null && tt.getAddDivision() != null)
                        pe.setClassInfo(new SchoolTimetableResponseDTO.ClassInfo(
                                tt.getAddStandard().getStandardId(), tt.getAddStandard().getStandard(),
                                tt.getAddDivision().getDivisionId(), tt.getAddDivision().getDivisionName()));
                    if (tt.getAddSubject() != null)
                        pe.setSubject(new SchoolTimetableResponseDTO.SubjectInfo(
                                tt.getAddSubject().getSubjectId(), tt.getAddSubject().getSubjectName()));
                    if (tt.getAddTeacher() != null)
                        pe.setTeacher(new SchoolTimetableResponseDTO.TeacherInfo(
                                tt.getAddTeacher().getId(), tt.getAddTeacher().getTeacherName()));
                    pe.setIsSubstitute(tt.getIsSubstitute() != null ? tt.getIsSubstitute() : false);
                    pe.setOriginalTeacher(tt.getOriginalTeacher() != null ? tt.getOriginalTeacher() : "");
                    pe.setIsCancelled(tt.getIsCancelled() != null ? tt.getIsCancelled() : false);

                    pe.setStatus(calculateStatus(tt.getStartTime(), tt.getEndTime(), requestedDate));

                    return pe;
                }).collect(Collectors.toList());

        SchoolTimetableResponseDTO response = new SchoolTimetableResponseDTO();
        response.setSchoolId(schoolId);
        response.setAcademicYearId(academicYearId);
        response.setDate(date);
        response.setDayOfWeek(dayOfWeek);
        response.setTimetable(periods);
        return response;
    }

    // ==================== UPDATE ====================
    @Override
    public TimetableDTO update(TimetableDTO dto) {
        checkForConflicts(dto, dto.getTimetableId());

        Timetable tt = timetableRepository.findByTimetableIdAndSchool_SchoolId(dto.getTimetableId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Timetable entry not found"));
        if (dto.getSubjectId() != null)
            tt.setAddSubject(subjectRepository.findBySubjectId(dto.getSubjectId()).orElse(null));
        if (dto.getTeacherId() != null)
            tt.setAddTeacher(teacherRepository.findById(dto.getTeacherId()).orElse(null));
        if (dto.getDayOfWeek() != null)
            tt.setDayOfWeek(dto.getDayOfWeek());
        if (dto.getStartTime() != null)
            tt.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null)
            tt.setEndTime(dto.getEndTime());
        if (dto.getPeriodNumber() != null)
            tt.setPeriodNumber(dto.getPeriodNumber());
        if (dto.getRoomNumber() != null)
            tt.setRoomNumber(dto.getRoomNumber());
        if (dto.getIsSubstitute() != null)
            tt.setIsSubstitute(dto.getIsSubstitute());
        if (dto.getOriginalTeacher() != null)
            tt.setOriginalTeacher(dto.getOriginalTeacher());
        if (dto.getIsCancelled() != null)
            tt.setIsCancelled(dto.getIsCancelled());

        return toDTO(timetableRepository.save(tt));
    }

    // ==================== DELETE ====================
    @Override
    public void delete(Long timetableId, Long schoolId) {
        Timetable tt = timetableRepository.findByTimetableIdAndSchool_SchoolId(timetableId, schoolId)
                .orElseThrow(() -> new RuntimeException("Timetable entry not found"));
        timetableRepository.delete(tt);
    }

    // ==================== GET TEACHER APP TIMETABLE ====================
    @Override
    public TeacherAppTimetableResponseDTO getTeacherAppTimetable(String type, String date, Long teacherId,
            Long schoolId, Long academicYearId) {
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate requestedDate = (date != null && !date.isBlank()) ? LocalDate.parse(date) : LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalDate todayDate = LocalDate.now();

        TeacherAppTimetableResponseDTO.PeriodConfig config = new TeacherAppTimetableResponseDTO.PeriodConfig();
        config.setTotalPeriods(8);
        config.setBreakAfterPeriod(4);

        if ("week".equalsIgnoreCase(type)) {
            DayOfWeek currentDay = requestedDate.getDayOfWeek();
            LocalDate startOfWeek = requestedDate.minusDays(currentDay.getValue() - 1);
            LocalDate endOfWeek = startOfWeek.plusDays(6);

            List<Timetable> allEntries = timetableRepository
                    .findBySchool_SchoolIdAndAddTeacher_IdAndAcademicYear_AcademicYearId(schoolId, teacherId,
                            academicYearId);

            List<TeacherAppTimetableResponseDTO.DaySchedule> days = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                LocalDate dDate = startOfWeek.plusDays(i);
                String dayName = dDate.getDayOfWeek().name();

                List<TeacherAppTimetableResponseDTO.ScheduleEntry> daySched = allEntries.stream()
                        .filter(tt -> dayName.equalsIgnoreCase(tt.getDayOfWeek()))
                        .sorted(Comparator.comparing((Timetable tt) -> tt.getStartTime(),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(tt -> mapToAppScheduleEntry(tt, timeFmt, dDate, todayDate, now, true))
                        .collect(Collectors.toList());

                TeacherAppTimetableResponseDTO.DaySchedule ds = new TeacherAppTimetableResponseDTO.DaySchedule();
                ds.setDate(dDate.toString());
                ds.setDayName(dayName.substring(0, 1) + dayName.substring(1).toLowerCase());
                ds.setDayStatus(daySched.isEmpty() ? "holiday" : "normal");
                ds.setSchedule(daySched);
                days.add(ds);
            }

            TeacherAppTimetableResponseDTO.WeekViewData weekData = new TeacherAppTimetableResponseDTO.WeekViewData();
            weekData.setType("week");
            weekData.setView("self");
            weekData.setStartDate(startOfWeek.toString());
            weekData.setEndDate(endOfWeek.toString());
            weekData.setPeriodConfig(config);
            weekData.setDays(days);

            TeacherAppTimetableResponseDTO response = new TeacherAppTimetableResponseDTO();
            response.setCode(200);
            response.setStatus("SUCCESS");
            response.setMessage("Timetable fetched");
            response.setData(weekData);
            return response;

        } else {
            String dayName = requestedDate.getDayOfWeek().name();
            List<Timetable> allEntries = timetableRepository
                    .findBySchool_SchoolIdAndAddTeacher_IdAndAcademicYear_AcademicYearId(schoolId, teacherId,
                            academicYearId);

            List<TeacherAppTimetableResponseDTO.ScheduleEntry> daySched = allEntries.stream()
                    .filter(tt -> dayName.equalsIgnoreCase(tt.getDayOfWeek()))
                    .sorted(Comparator.comparing((Timetable tt) -> tt.getStartTime(),
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(tt -> mapToAppScheduleEntry(tt, timeFmt, requestedDate, todayDate, now, true))
                    .collect(Collectors.toList());

            TeacherAppTimetableResponseDTO.DayViewData dayData = new TeacherAppTimetableResponseDTO.DayViewData();
            dayData.setType("day");
            dayData.setView("self");
            dayData.setDate(requestedDate.toString());
            dayData.setIsToday(requestedDate.equals(todayDate));
            dayData.setServerTime(now.format(timeFmt));
            dayData.setDayStatus(daySched.isEmpty() ? "holiday" : "normal");
            dayData.setPeriodConfig(config);
            dayData.setSchedule(daySched);

            TeacherAppTimetableResponseDTO response = new TeacherAppTimetableResponseDTO();
            response.setCode(200);
            response.setStatus("SUCCESS");
            response.setMessage("Timetable fetched");
            response.setData(dayData);
            return response;
        }
    }

    private TeacherAppTimetableResponseDTO.ScheduleEntry mapToAppScheduleEntry(Timetable tt, DateTimeFormatter timeFmt,
            LocalDate targetDate, LocalDate todayDate, LocalTime now, boolean includeClassInfo) {
        TeacherAppTimetableResponseDTO.ScheduleEntry se = new TeacherAppTimetableResponseDTO.ScheduleEntry();
        se.setPeriodId(tt.getTimetableId());
        se.setStartTime(tt.getStartTime() != null ? tt.getStartTime().format(timeFmt) : "");
        se.setEndTime(tt.getEndTime() != null ? tt.getEndTime().format(timeFmt) : "");

        if (includeClassInfo) {
            se.setClassName(tt.getAddStandard() != null ? tt.getAddStandard().getStandard() : "");
            se.setDivision(tt.getAddDivision() != null ? tt.getAddDivision().getDivisionName() : "");
        } else {
            se.setClassName(null);
            se.setDivision(null);
        }

        se.setSubject(tt.getAddSubject() != null ? tt.getAddSubject().getSubjectName() : "");
        se.setRoom(tt.getRoomNumber() != null ? tt.getRoomNumber() : "");
        se.setIsSubstitute(tt.getIsSubstitute() != null ? tt.getIsSubstitute() : false);
        se.setOriginalTeacher(tt.getOriginalTeacher() != null ? tt.getOriginalTeacher() : "");
        se.setIsCancelled(tt.getIsCancelled() != null ? tt.getIsCancelled() : false);

        se.setStatus(calculateStatus(tt.getStartTime(), tt.getEndTime(), targetDate));
        return se;
    }

    // ==================== GET STUDENT TIMETABLE (DAY / WEEK) ====================
    @Override
    public StudentAppTimetableResponseDTO getStudentTimetable(String type, String date, Long studentId,
            Long schoolId, Long academicYearId) {
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate requestedDate = (date != null && !date.isBlank()) ? LocalDate.parse(date) : LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalDate todayDate = LocalDate.now();

        // 1. Look up student to get standard and division
        AddStudent student = addStudentRepository.findByIdAndSchool_SchoolId(studentId, schoolId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getAddStandard() == null || student.getAddDivision() == null) {
            throw new RuntimeException("Student is not assigned to a class/division");
        }

        Long standardId = student.getAddStandard().getStandardId();
        Long divisionId = student.getAddDivision().getDivisionId();

        // 2. Query timetable for the student's class
        List<Timetable> allEntries = timetableRepository
                .findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndAcademicYear_AcademicYearId(
                        schoolId, standardId, divisionId, academicYearId);

        if ("week".equalsIgnoreCase(type)) {
            // ==================== WEEK VIEW ====================
            DayOfWeek currentDay = requestedDate.getDayOfWeek();
            LocalDate startOfWeek = requestedDate.minusDays(currentDay.getValue() - 1);
            LocalDate endOfWeek = startOfWeek.plusDays(6);

            List<StudentAppTimetableResponseDTO.DaySchedule> days = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                LocalDate dDate = startOfWeek.plusDays(i);
                String dayName = dDate.getDayOfWeek().name();

                List<StudentAppTimetableResponseDTO.ScheduleEntry> daySched = allEntries.stream()
                        .filter(tt -> dayName.equalsIgnoreCase(tt.getDayOfWeek()))
                        .sorted(Comparator.comparing((Timetable tt) -> tt.getStartTime(),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(tt -> mapToStudentScheduleEntry(tt, timeFmt, dDate))
                        .collect(Collectors.toList());

                StudentAppTimetableResponseDTO.DaySchedule ds = new StudentAppTimetableResponseDTO.DaySchedule();
                ds.setDate(dDate.toString());
                ds.setDayName(dayName.substring(0, 1) + dayName.substring(1).toLowerCase());
                ds.setDayStatus(daySched.isEmpty() ? "holiday" : "normal");
                ds.setSchedule(daySched);
                days.add(ds);
            }

            StudentAppTimetableResponseDTO.WeekViewData weekData = new StudentAppTimetableResponseDTO.WeekViewData();
            weekData.setType("week");
            weekData.setStartDate(startOfWeek.toString());
            weekData.setEndDate(endOfWeek.toString());
            weekData.setDays(days);

            StudentAppTimetableResponseDTO response = new StudentAppTimetableResponseDTO();
            response.setCode(200);
            response.setStatus("SUCCESS");
            response.setMessage("Student timetable fetched");
            response.setData(weekData);
            return response;

        } else {
            // ==================== DAY VIEW ====================
            String dayName = requestedDate.getDayOfWeek().name();

            List<StudentAppTimetableResponseDTO.ScheduleEntry> daySched = allEntries.stream()
                    .filter(tt -> dayName.equalsIgnoreCase(tt.getDayOfWeek()))
                    .sorted(Comparator.comparing((Timetable tt) -> tt.getStartTime(),
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(tt -> mapToStudentScheduleEntry(tt, timeFmt, requestedDate))
                    .collect(Collectors.toList());

            StudentAppTimetableResponseDTO.DayViewData dayData = new StudentAppTimetableResponseDTO.DayViewData();
            dayData.setType("day");
            dayData.setDate(requestedDate.toString());
            dayData.setIsToday(requestedDate.equals(todayDate));
            dayData.setServerTime(now.format(timeFmt));
            dayData.setDayStatus(daySched.isEmpty() ? "holiday" : "normal");
            dayData.setSchedule(daySched);

            StudentAppTimetableResponseDTO response = new StudentAppTimetableResponseDTO();
            response.setCode(200);
            response.setStatus("SUCCESS");
            response.setMessage("Student timetable fetched");
            response.setData(dayData);
            return response;
        }
    }

    private StudentAppTimetableResponseDTO.ScheduleEntry mapToStudentScheduleEntry(Timetable tt,
            DateTimeFormatter timeFmt, LocalDate targetDate) {
        StudentAppTimetableResponseDTO.ScheduleEntry se = new StudentAppTimetableResponseDTO.ScheduleEntry();
        se.setPeriodId(tt.getTimetableId());
        se.setPeriodNumber(tt.getPeriodNumber());
        se.setStartTime(tt.getStartTime() != null ? tt.getStartTime().format(timeFmt) : "");
        se.setEndTime(tt.getEndTime() != null ? tt.getEndTime().format(timeFmt) : "");
        se.setSubject(tt.getAddSubject() != null ? tt.getAddSubject().getSubjectName() : "");
        se.setTeacher(tt.getAddTeacher() != null ? tt.getAddTeacher().getTeacherName() : "");
        se.setRoom(tt.getRoomNumber() != null ? tt.getRoomNumber() : "");
        se.setIsSubstitute(tt.getIsSubstitute() != null ? tt.getIsSubstitute() : false);
        se.setOriginalTeacher(tt.getOriginalTeacher() != null ? tt.getOriginalTeacher() : "");
        se.setIsCancelled(tt.getIsCancelled() != null ? tt.getIsCancelled() : false);
        se.setStatus(calculateStatus(tt.getStartTime(), tt.getEndTime(), targetDate));
        return se;
    }
}