package model;

/* Status of a fixture. A fixture starts as SCHEDULED and only becomes
   COMPLETED once a valid result has been recorded (BR8). */
public enum FixtureStatus {
    SCHEDULED, COMPLETED
}
