package com.uconnect.backend.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import com.uconnect.backend.connect.dao.ConnectDAO;
import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.helper.BaseUnitTest;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestConnectService extends BaseUnitTest {
    @Mock
    private ConnectDAO dao;

    private ConnectService connectService;

    private User sender;

    private User receiver;

    private User current;

    private User other;

    private boolean init = false;

    @BeforeEach
    public void setup() {
        if (!init) {
            sender = MockData.generateValidUser();
            receiver = MockData.generateValidUser();
            current = MockData.generateValidUser();
            other = MockData.generateValidUser();
            connectService = new ConnectService(dao);
            init = true;
        }
    } 

    // request()
    private void setRequestExitCode(int exitCode) throws UserNotFoundException {
        when(dao.request(sender.getUsername(), receiver.getUsername()))
            .thenReturn(exitCode);
    }

    private void assertRequestExitCodeEquals(int exitCode) {
        assertEquals(exitCode, connectService.request(sender.getUsername(), receiver.getUsername()));
    }

    @Test
    public void testRequest() throws UserNotFoundException {
        setRequestExitCode(0);
        assertRequestExitCodeEquals(0);
    }

    @Test
    public void testRequestAlreadySent() throws UserNotFoundException {
        setRequestExitCode(-1);
        assertRequestExitCodeEquals(-1);
    }

    @Test
    public void testRequestAlreadyReceived() throws UserNotFoundException {
        setRequestExitCode(-2);
        assertRequestExitCodeEquals(-2);
    }

    @Test
    public void testRequestNotEnoughRequests() throws UserNotFoundException {
        setRequestExitCode(-3);
        assertRequestExitCodeEquals(-3);
    }

    @Test
    public void testRequestUserNotFound() throws UserNotFoundException {
        when(dao.request(sender.getUsername(), receiver.getUsername()))
            .thenThrow(UserNotFoundException.class);
        assertRequestExitCodeEquals(-4);
    }

    @Test
    public void testRequestUnexpectedException() throws UserNotFoundException {
        when(dao.request(sender.getUsername(), receiver.getUsername()))
            .thenThrow(RuntimeException.class);
        assertRequestExitCodeEquals(-5);
    }

    // undoRequest()
    private void setUndoRequestExitCode(int exitCode) throws UserNotFoundException {
        when(dao.undoRequest(sender.getUsername(), receiver.getUsername()))
            .thenReturn(exitCode);
    }

    private void assertUndoRequestExitCodeEquals(int exitCode) {
        assertEquals(exitCode, connectService.undoRequest(sender.getUsername(), receiver.getUsername()));
    }

    @Test
    public void testUndoRequest() throws UserNotFoundException {
        setUndoRequestExitCode(0);
        assertUndoRequestExitCodeEquals(0);
    }

    @Test
    public void testUndoRequestNeverSent() throws UserNotFoundException {
        setUndoRequestExitCode(-1);
        assertUndoRequestExitCodeEquals(-1);
    }

    @Test
    public void testUndoRequestNeverReceived() throws UserNotFoundException {
        setUndoRequestExitCode(-2);
        assertUndoRequestExitCodeEquals(-2);
    }

    @Test
    public void testUndoRequestTooManyRequests() throws UserNotFoundException {
        setUndoRequestExitCode(-3);
        assertUndoRequestExitCodeEquals(-3);
    }

    @Test
    public void testUndoRequestUserNotFound() throws UserNotFoundException {
        when(dao.undoRequest(sender.getUsername(), receiver.getUsername()))
            .thenThrow(UserNotFoundException.class);
        assertUndoRequestExitCodeEquals(-4);
    } 

    @Test
    public void testUndoRequestUnexpectedException() throws UserNotFoundException {
        when(dao.undoRequest(sender.getUsername(), receiver.getUsername()))
            .thenThrow(RuntimeException.class);
        assertUndoRequestExitCodeEquals(-5);
    }

    // accept()
    private void setAcceptExitCode(int exitCode) throws UserNotFoundException {
        when(dao.accept(sender.getUsername(), receiver.getUsername()))
            .thenReturn(exitCode);
    }

    private void assertAcceptExitCodeEquals(int exitCode) {
        assertEquals(exitCode, connectService.accept(sender.getUsername(), receiver.getUsername()));
    }

    @Test
    public void testAccept() throws UserNotFoundException {
        setAcceptExitCode(0);
        assertAcceptExitCodeEquals(0);
    }

    @Test
    public void testAcceptNeverReceived() throws UserNotFoundException {
        setAcceptExitCode(-1);
        assertAcceptExitCodeEquals(-1);
    }

    @Test
    public void testAcceptNeverSent() throws UserNotFoundException {
        setAcceptExitCode(-2);
        assertAcceptExitCodeEquals(-2);
    }

    @Test
    public void testAcceptReceiverAlreadyConnected() throws UserNotFoundException {
        setAcceptExitCode(-3);
        assertAcceptExitCodeEquals(-3);
    }

    @Test
    public void testAcceptSenderAlreadyConnected() throws UserNotFoundException {
        setAcceptExitCode(-4);
        assertAcceptExitCodeEquals(-4);
    }

    @Test
    public void testAcceptTooManyRequests() throws UserNotFoundException {
        setAcceptExitCode(-5);
        assertAcceptExitCodeEquals(-5);
    }

    @Test
    public void testAcceptUserNotFound() throws UserNotFoundException {
        when(dao.accept(sender.getUsername(), receiver.getUsername()))
            .thenThrow(UserNotFoundException.class);
        assertAcceptExitCodeEquals(-6);
    }

    @Test
    public void testAcceptUnexpectedException() throws UserNotFoundException {
        when(dao.accept(sender.getUsername(), receiver.getUsername()))
            .thenThrow(RuntimeException.class);
        assertAcceptExitCodeEquals(-7);
    }

    // checkStatus()
    private void setCheckStatusExitCode(int exitCode) throws UserNotFoundException {
        when(dao.checkStatus(current.getUsername(), other.getUsername()))
            .thenReturn(exitCode);
    }

    private void assertCheckStatusExitCodeEquals(int exitCode) {
        assertEquals(exitCode, connectService.checkStatus(current.getUsername(), other.getUsername()));
    }

    @Test
    public void testCheckStatusNoRelation() throws UserNotFoundException {
        setCheckStatusExitCode(0);
        assertCheckStatusExitCodeEquals(0);
    }

    @Test
    public void testCheckStatusConnected() throws UserNotFoundException {
        setCheckStatusExitCode(1);
        assertCheckStatusExitCodeEquals(1);
    }

    @Test
    public void testCheckStatusOutgoingRequest() throws UserNotFoundException {
        setCheckStatusExitCode(2);
        assertCheckStatusExitCodeEquals(2);
    }

    @Test
    public void testCheckStatusIncomingRequest() throws UserNotFoundException {
        setCheckStatusExitCode(3);
        assertCheckStatusExitCodeEquals(3);
    }
    
    @Test
    public void testCheckStatusUserNotFound() throws UserNotFoundException {
        when(dao.checkStatus(current.getUsername(), other.getUsername()))
            .thenThrow(UserNotFoundException.class);
        assertCheckStatusExitCodeEquals(-1);
    }

    @Test
    public void testCheckStatusUnexpectedException() throws UserNotFoundException {
        when(dao.checkStatus(current.getUsername(), other.getUsername()))
            .thenThrow(RuntimeException.class);
        assertCheckStatusExitCodeEquals(-2);
    }
}
