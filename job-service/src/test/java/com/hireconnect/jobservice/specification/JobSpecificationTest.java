package com.hireconnect.jobservice.specification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import com.hireconnect.jobservice.entity.ExperienceLevel;
import com.hireconnect.jobservice.entity.Job;
import com.hireconnect.jobservice.entity.JobStatus;
import com.hireconnect.jobservice.entity.JobType;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

class JobSpecificationTest {

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void filterOpenJobs_AllParameters() {
        Root root = mock(Root.class);
        CriteriaQuery query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);
        Expression expression = mock(Expression.class);

        when(root.get(anyString())).thenReturn(path);
        when(cb.equal(any(), any())).thenReturn(predicate);
        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.lower(any())).thenReturn(expression);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        when(cb.greaterThanOrEqualTo(any(), any(Double.class))).thenReturn(predicate);
        when(cb.lessThanOrEqualTo(any(), any(Double.class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);
        when(cb.desc(any())).thenReturn(mock(jakarta.persistence.criteria.Order.class));

        Specification<Job> spec = JobSpecification.filterOpenJobs(
                "java", "london", JobType.FULL_TIME, ExperienceLevel.SENIOR, 1000.0, 5000.0
        );

        spec.toPredicate(root, query, cb);

        verify(cb, atLeastOnce()).equal(any(), org.mockito.ArgumentMatchers.eq(JobStatus.OPEN));
    }
}
