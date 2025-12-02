package com.example.otams;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Local unit tests for OTAMS project.
 * MUST BE LOCATED IN: app/src/test/java/com/example/otams/
 */
public class ExampleUnitTest {

    @Test
    public void sessionRequest_constructor_assignments() {
        String slotId = "slot_test_1";
        String tutorId = "tutor_test_1";
        String studentId = "student_test_1";
        String studentName = "Jane Doe";

        SessionRequest request = new SessionRequest(slotId, tutorId, studentId, studentName);

        assertEquals("Jane Doe", request.getStudentName());
        assertEquals(slotId, request.getSlotID());
        assertEquals(tutorId, request.getTutorUID());
    }

    @Test
    public void sessionRequest_default_status() {
        SessionRequest request = new SessionRequest("s1", "t1", "st1", "Name");
        assertEquals("pending", request.getBookingStatus());
        assertNotNull(request.getRequestTimeStamp());
    }

    @Test
    public void sessionRequest_state_change() {
        SessionRequest request = new SessionRequest();
        request.setBookingStatus("approved");
        request.setRequestID("REQ_999");

        assertEquals("approved", request.getBookingStatus());
        assertEquals("REQ_999", request.getRequestID());
    }

    @Test
    public void inheritance_check() {
        Tutor tutor = new Tutor("Tutor", "Name", "email@test.com", "pass", "1234567890", "PhD", "SEG2105", false, false, true);
        assertTrue(tutor instanceof User);
        assertNotNull(tutor);
    }
}