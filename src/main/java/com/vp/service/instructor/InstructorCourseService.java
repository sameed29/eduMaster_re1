package com.vp.service.instructor;

import com.vp.entity.Course;
import com.vp.repository.CourseRepository;
import com.vp.service.common.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing instructor courses
 */
@Service
@Transactional(readOnly = true)
public class InstructorCourseService {

    private static final Logger logger = LoggerFactory.getLogger(InstructorCourseService.class);

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private FileUploadService fileUploadService;

    // ==================== CREATE COURSE ====================

    @Transactional
    public Course createCourse(Course course, MultipartFile thumbnail, MultipartFile instructorPhoto) {
        try {
            course.setCreatedAt(LocalDateTime.now());
            course.setUpdatedAt(LocalDateTime.now());

            if (course.getAverageRating() == null) course.setAverageRating(0.0);
            if (course.getTotalEnrollments() == null) course.setTotalEnrollments(0);
            if (course.getTotalReviews() == null) course.setTotalReviews(0);

            if (thumbnail != null && !thumbnail.isEmpty()) {
                logger.info("Uploading course thumbnail for: {}", course.getTitle());
                String thumbnailUrl = fileUploadService.uploadFile(thumbnail, "courses/thumbnails");
                course.setThumbnailUrl(thumbnailUrl);
            }

            if (instructorPhoto != null && !instructorPhoto.isEmpty()) {
                logger.info("Uploading instructor photo for course: {}", course.getTitle());
                String photoUrl = fileUploadService.uploadFile(instructorPhoto, "instructors/photos");
                course.setInstructorPhotoUrl(photoUrl);
            }

            Course savedCourse = courseRepository.save(course);
            logger.info("✅ Course created successfully - ID: {}, Title: {}", savedCourse.getId(), savedCourse.getTitle());

            return savedCourse;

        } catch (Exception e) {
            logger.error("❌ Failed to create course: {}", course.getTitle(), e);
            throw new RuntimeException("Failed to create course: " + e.getMessage(), e);
        }
    }

    // ==================== UPDATE COURSE ====================

    @Transactional
    public Course updateCourse(Course course, MultipartFile thumbnail, MultipartFile instructorPhoto) {
        try {
            Course existingCourse = getCourseById(course.getId());

            course.setUpdatedAt(LocalDateTime.now());
            course.setCreatedAt(existingCourse.getCreatedAt());

            if (thumbnail != null && !thumbnail.isEmpty()) {
                logger.info("Uploading new thumbnail for course: {}", course.getTitle());
                if (existingCourse.getThumbnailUrl() != null) {
                    try {
                        fileUploadService.deleteFile(existingCourse.getThumbnailUrl());
                    } catch (Exception e) {
                        logger.warn("Failed to delete old thumbnail: {}", e.getMessage());
                    }
                }
                String thumbnailUrl = fileUploadService.uploadFile(thumbnail, "courses/thumbnails");
                course.setThumbnailUrl(thumbnailUrl);
            } else {
                course.setThumbnailUrl(existingCourse.getThumbnailUrl());
            }

            if (instructorPhoto != null && !instructorPhoto.isEmpty()) {
                logger.info("Uploading new instructor photo for course: {}", course.getTitle());
                if (existingCourse.getInstructorPhotoUrl() != null) {
                    try {
                        fileUploadService.deleteFile(existingCourse.getInstructorPhotoUrl());
                    } catch (Exception e) {
                        logger.warn("Failed to delete old instructor photo: {}", e.getMessage());
                    }
                }
                String photoUrl = fileUploadService.uploadFile(instructorPhoto, "instructors/photos");
                course.setInstructorPhotoUrl(photoUrl);
            } else {
                course.setInstructorPhotoUrl(existingCourse.getInstructorPhotoUrl());
            }

            Course updatedCourse = courseRepository.save(course);
            logger.info("✅ Course updated successfully - ID: {}, Title: {}", updatedCourse.getId(), updatedCourse.getTitle());

            return updatedCourse;

        } catch (Exception e) {
            logger.error("❌ Failed to update course ID: {}", course.getId(), e);
            throw new RuntimeException("Failed to update course: " + e.getMessage(), e);
        }
    }

    // ==================== SAVE COURSE ====================

    @Transactional
    public Course saveCourse(Course course) {
        try {
            course.setUpdatedAt(LocalDateTime.now());
            Course savedCourse = courseRepository.save(course);
            logger.info("✅ Course saved - ID: {}, Status: {}", savedCourse.getId(), savedCourse.getStatus());
            return savedCourse;
        } catch (Exception e) {
            logger.error("❌ Failed to save course ID: {}", course.getId(), e);
            throw new RuntimeException("Failed to save course: " + e.getMessage(), e);
        }
    }

    // ==================== RETRIEVE COURSES (READ-ONLY) ====================

    public List<Course> getCoursesByInstructor(String instructorEmail) {
        List<Course> courses = courseRepository.findByInstructorEmail(instructorEmail);
        logger.info("Retrieved {} courses for instructor: {}", courses.size(), instructorEmail);
        return courses;
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Course not found with ID: {}", id);
                    return new RuntimeException("Course not found with ID: " + id);
                });
    }

    public List<Course> getLiveCourses() {
        List<Course> courses = courseRepository.findPublishedCourses();
        logger.info("Retrieved {} published courses for homepage", courses.size());
        return courses;
    }

    public List<Course> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        logger.info("Retrieved all {} courses", courses.size());
        return courses;
    }

    // ==================== DELETE COURSE ====================

    @Transactional
    public void deleteCourse(Long id) {
        try {
            Course course = getCourseById(id);

            if (course.getThumbnailUrl() != null) {
                try {
                    fileUploadService.deleteFile(course.getThumbnailUrl());
                } catch (Exception e) {
                    logger.warn("Failed to delete thumbnail: {}", e.getMessage());
                }
            }

            if (course.getInstructorPhotoUrl() != null) {
                try {
                    fileUploadService.deleteFile(course.getInstructorPhotoUrl());
                } catch (Exception e) {
                    logger.warn("Failed to delete instructor photo: {}", e.getMessage());
                }
            }

            courseRepository.deleteById(id);
            logger.info("✅ Course deleted successfully - ID: {}, Title: {}", id, course.getTitle());

        } catch (Exception e) {
            logger.error("❌ Failed to delete course ID: {}", id, e);
            throw new RuntimeException("Failed to delete course: " + e.getMessage(), e);
        }
    }

    // ==================== SEARCH & FILTER (READ-ONLY) ====================

    public List<Course> getCoursesByCategory(String category) {
        return courseRepository.findByCategory(category);
    }

    public List<Course> getCoursesByStatus(String status) {
        return courseRepository.findByStatus(status);
    }

    public List<Course> searchCourses(String keyword) {
        return courseRepository.searchByKeyword(keyword);
    }

    // ==================== STATISTICS & VALIDATION (READ-ONLY) ====================

    public long getCourseCountByInstructor(String instructorEmail) {
        Long count = courseRepository.countByInstructorEmail(instructorEmail);
        return count != null ? count : 0;
    }

    public boolean courseExists(Long id) {
        return courseRepository.existsById(id);
    }

    public boolean isInstructorOwner(Long courseId, String instructorEmail) {
        try {
            Course course = getCourseById(courseId);
            return course.getInstructorEmail().equals(instructorEmail);
        } catch (Exception e) {
            return false;
        }
    }
}