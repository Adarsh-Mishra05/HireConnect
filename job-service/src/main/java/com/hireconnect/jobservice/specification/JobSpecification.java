package com.hireconnect.jobservice.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.hireconnect.jobservice.entity.ExperienceLevel;
import com.hireconnect.jobservice.entity.Job;
import com.hireconnect.jobservice.entity.JobStatus;
import com.hireconnect.jobservice.entity.JobType;

import jakarta.persistence.criteria.Predicate;

public class JobSpecification {

    public static Specification<Job> filterOpenJobs(
            String keyword,
            String location,
            JobType jobType,
            ExperienceLevel experienceLevel,
            Double minSalary,
            Double maxSalary
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("status"), JobStatus.OPEN));

            if (StringUtils.hasText(keyword)) {
                String likeKeyword = "%" + keyword.toLowerCase() + "%";
                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likeKeyword),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likeKeyword),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("companyName")), likeKeyword),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("skillsRequired")), likeKeyword)
                        )
                );
            }

            if (StringUtils.hasText(location)) {
                predicates.add(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("location")),
                                "%" + location.toLowerCase() + "%")
                );
            }

            if (jobType != null) {
                predicates.add(criteriaBuilder.equal(root.get("jobType"), jobType));
            }

            if (experienceLevel != null) {
                predicates.add(criteriaBuilder.equal(root.get("experienceLevel"), experienceLevel));
            }

            if (minSalary != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("salaryMin"), minSalary));
            }

            if (maxSalary != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("salaryMax"), maxSalary));
            }

            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}