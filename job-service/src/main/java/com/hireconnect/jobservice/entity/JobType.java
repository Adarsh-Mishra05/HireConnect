package com.hireconnect.jobservice.entity;

/*
 * Enums are fixed sets of values (like constants).
 * They restrict data to only valid options → no random/invalid values in DB.
 * Prevents messy + inconsistent data 
 */
public enum JobType {
    FULL_TIME,
    PART_TIME,
    INTERNSHIP,
    CONTRACT
}
