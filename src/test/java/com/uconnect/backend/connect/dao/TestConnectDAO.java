package com.uconnect.backend.connect.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import java.util.HashSet;
import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.helper.BaseUnitTest;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

@ExtendWith(MockitoExtension.class)
public class TestConnectDAO extends BaseUnitTest {

    @Autowired
    private String userTableName;

    @Mock
    private DdbAdapter ddbAdapter;

    private ConnectDAO dao;

    private User sender;

    private User receiver;

    private User current;

    private User other;

    private boolean init = false;

    @BeforeEach
    public void setup() {
        if (!init) {
            current = MockData.generateValidUser();
            current.setPending(new HashSet<>());
            current.setConnections(new HashSet<>());
            current.setSent(new HashSet<>());

            other = MockData.generateValidUser();
            other.setPending(new HashSet<>());
            other.setConnections(new HashSet<>());
            other.setSent(new HashSet<>());

            dao = new ConnectDAO(ddbAdapter, userTableName);

            init = true;
        }
        sender = MockData.generateValidUser();
        sender.setRequests(5);
        sender.setPending(new HashSet<>());
        sender.setConnections(new HashSet<>());
        sender.setSent(new HashSet<>());

        receiver = MockData.generateValidUser();
        receiver.setRequests(5);
        receiver.setPending(new HashSet<>());
        receiver.setConnections(new HashSet<>());
        receiver.setSent(new HashSet<>());
    }

    // request()
    @Test
    public void testRequest() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(sender.getUsername())).thenReturn(sender);
        when(ddbAdapter.findByUsername(receiver.getUsername())).thenReturn(receiver);

        assertTrue(!sender.getSent().contains(receiver.getUsername()));
        assertTrue(!receiver.getPending().contains(sender.getUsername()));
        assertTrue(sender.getRequests() >= 1);
        assertEquals(0, dao.request(sender.getUsername(), receiver.getUsername()));
    }

    @Test
    public void testRequestAlreadySent() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(sender.getUsername())).thenReturn(sender);
        when(ddbAdapter.findByUsername(receiver.getUsername())).thenReturn(receiver);

        sender.getSent().add(receiver.getUsername());

        assertTrue(sender.getSent().contains(receiver.getUsername()));
        assertEquals(-1, dao.request(sender.getUsername(), receiver.getUsername()));
    }

    @Test
    public void testRequestAlreadyReceived() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(sender.getUsername())).thenReturn(sender);
        when(ddbAdapter.findByUsername(receiver.getUsername())).thenReturn(receiver);

        receiver.getPending().add(sender.getUsername());

        assertTrue(!sender.getSent().contains(receiver.getUsername()));
        assertTrue(receiver.getPending().contains(sender.getUsername()));
        assertEquals(-2, dao.request(sender.getUsername(), receiver.getUsername()));
    }

    @Test
    public void testRequestNotEnoughRequests() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(sender.getUsername())).thenReturn(sender);
        when(ddbAdapter.findByUsername(receiver.getUsername())).thenReturn(receiver);

        sender.setRequests(0);

        assertTrue(!sender.getSent().contains(receiver.getUsername()));
        assertTrue(!receiver.getPending().contains(sender.getUsername()));
        assertTrue(sender.getRequests() < 1);
        assertEquals(-3, dao.request(sender.getUsername(), receiver.getUsername()));
    }

    @Test
    public void testRequestUserNotFound() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(sender.getUsername()))
            .thenThrow(UserNotFoundException.class);
        assertThrows(UserNotFoundException.class,
            () -> dao.request(sender.getUsername(), receiver.getUsername()));
    }

    // undoRequest()
    @Test
    public void testUndoRequest() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(sender.getUsername())).thenReturn(sender);
        when(ddbAdapter.findByUsername(receiver.getUsername())).thenReturn(receiver);

        sender.getSent().add(receiver.getUsername());
        receiver.getPending().add(sender.getUsername());

        assertTrue(sender.getSent().contains(receiver.getUsername()));
        assertTrue(receiver.getPending().contains(sender.getUsername()));
        assertTrue(sender.getRequests() <= 9);
        assertEquals(0, dao.undoRequest(sender.getUsername(), receiver.getUsername()));
    }

    @Test
    public void testUndoRequestNeverSent() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(sender.getUsername())).thenReturn(sender);
        when(ddbAdapter.findByUsername(receiver.getUsername())).thenReturn(receiver);

        assertTrue(!sender.getSent().contains(receiver.getUsername()));
        assertEquals(-1, dao.undoRequest(sender.getUsername(), receiver.getUsername()));
    }

    @Test
    public void testUndoRequestNeverReceived() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(sender.getUsername())).thenReturn(sender);
        when(ddbAdapter.findByUsername(receiver.getUsername())).thenReturn(receiver);

        sender.getSent().add(receiver.getUsername());

        assertTrue(sender.getSent().contains(receiver.getUsername()));
        assertTrue(!receiver.getPending().contains(sender.getUsername()));
        assertEquals(-2, dao.undoRequest(sender.getUsername(), receiver.getUsername()));
    }

    @Test
    public void testUndoRequestTooManyRequests() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(sender.getUsername())).thenReturn(sender);
        when(ddbAdapter.findByUsername(receiver.getUsername())).thenReturn(receiver);

        sender.getSent().add(receiver.getUsername());
        receiver.getPending().add(sender.getUsername());
        sender.setRequests(10);

        assertTrue(sender.getSent().contains(receiver.getUsername()));
        assertTrue(receiver.getPending().contains(sender.getUsername()));
        assertTrue(sender.getRequests() > 9);
        assertEquals(-3, dao.undoRequest(sender.getUsername(), receiver.getUsername()));
    }

    @Test
    public void testUndoRequestUserNotFound() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(sender.getUsername()))
            .thenThrow(UserNotFoundException.class);
        assertThrows(UserNotFoundException.class, 
            () -> dao.undoRequest(sender.getUsername(), receiver.getUsername()));
    }

    // accept()
    @Test
    public void testAccept() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(sender.getUsername())).thenReturn(sender);
        when(ddbAdapter.findByUsername(receiver.getUsername())).thenReturn(receiver);

        receiver.getPending().add(sender.getUsername());
        sender.getSent().add(receiver.getUsername());

        assertTrue(receiver.getPending().contains(sender.getUsername()));
        assertTrue(sender.getSent().contains(receiver.getUsername()));
        assertTrue(!receiver.getConnections().contains(sender.getUsername()));
        assertTrue(!sender.getConnections().contains(receiver.getUsername()));
        assertTrue(sender.getRequests() <= 9);
        assertEquals(0, dao.accept(sender.getUsername(), receiver.getUsername()));
    }

    @Test
    public void testAcceptNeverReceived() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(sender.getUsername())).thenReturn(sender);
        when(ddbAdapter.findByUsername(receiver.getUsername())).thenReturn(receiver);

        assertTrue(!receiver.getPending().contains(sender.getUsername()));
        assertEquals(-1, dao.accept(sender.getUsername(), receiver.getUsername()));
    }

    @Test
    public void testAcceptNeverSent() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(sender.getUsername())).thenReturn(sender);
        when(ddbAdapter.findByUsername(receiver.getUsername())).thenReturn(receiver);

        receiver.getPending().add(sender.getUsername());

        assertTrue(receiver.getPending().contains(sender.getUsername()));
        assertTrue(!sender.getSent().contains(receiver.getUsername()));
        assertEquals(-2, dao.accept(sender.getUsername(), receiver.getUsername()));
    }

    @Test
    public void testAcceptReceiverAlreadyConnected() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(sender.getUsername())).thenReturn(sender);
        when(ddbAdapter.findByUsername(receiver.getUsername())).thenReturn(receiver);

        receiver.getPending().add(sender.getUsername());
        sender.getSent().add(receiver.getUsername());
        receiver.getConnections().add(sender.getUsername());

        assertTrue(receiver.getPending().contains(sender.getUsername()));
        assertTrue(sender.getSent().contains(receiver.getUsername()));
        assertTrue(receiver.getConnections().contains(sender.getUsername()));
        assertEquals(-3, dao.accept(sender.getUsername(), receiver.getUsername()));
    }

    @Test
    public void testAcceptSenderAlreadyConnected() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(sender.getUsername())).thenReturn(sender);
        when(ddbAdapter.findByUsername(receiver.getUsername())).thenReturn(receiver);

        receiver.getPending().add(sender.getUsername());
        sender.getSent().add(receiver.getUsername());
        sender.getConnections().add(receiver.getUsername());

        assertTrue(receiver.getPending().contains(sender.getUsername()));
        assertTrue(sender.getSent().contains(receiver.getUsername()));
        assertTrue(!receiver.getConnections().contains(sender.getUsername()));
        assertTrue(sender.getConnections().contains(receiver.getUsername()));
        assertEquals(-4, dao.accept(sender.getUsername(), receiver.getUsername()));
    }

    @Test
    public void testAcceptTooManyRequests() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(sender.getUsername())).thenReturn(sender);
        when(ddbAdapter.findByUsername(receiver.getUsername())).thenReturn(receiver);

        receiver.getPending().add(sender.getUsername());
        sender.getSent().add(receiver.getUsername());
        sender.setRequests(10);

        assertTrue(receiver.getPending().contains(sender.getUsername()));
        assertTrue(sender.getSent().contains(receiver.getUsername()));
        assertTrue(!receiver.getConnections().contains(sender.getUsername()));
        assertTrue(!sender.getConnections().contains(receiver.getUsername()));
        assertTrue(sender.getRequests() > 9);
        assertEquals(-5, dao.accept(sender.getUsername(), receiver.getUsername()));
    }

    @Test
    public void testAcceptUserNotFound() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(sender.getUsername()))
            .thenThrow(UserNotFoundException.class);
        assertThrows(UserNotFoundException.class,
            () -> dao.accept(sender.getUsername(), receiver.getUsername()));
    }

    // checkStatus()
    @Test
    public void testCheckStatusNoRelation() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(current.getUsername())).thenReturn(current);

        assertTrue(!current.getConnections().contains(other.getUsername()));
        assertTrue(!current.getSent().contains(other.getUsername()));
        assertTrue(!current.getPending().contains(other.getUsername()));
        assertEquals(0, dao.checkStatus(current.getUsername(), other.getUsername()));
    }

    @Test
    public void testCheckStatusConnected() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(current.getUsername())).thenReturn(current);

        current.getConnections().add(other.getUsername());

        assertTrue(current.getConnections().contains(other.getUsername()));
        assertEquals(1, dao.checkStatus(current.getUsername(), other.getUsername()));
    }

    @Test
    public void testCheckStatusOutgoingRequest() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(current.getUsername())).thenReturn(current);

        current.getSent().add(other.getUsername());

        assertTrue(!current.getConnections().contains(other.getUsername()));
        assertTrue(current.getSent().contains(other.getUsername()));
        assertEquals(2, dao.checkStatus(current.getUsername(), other.getUsername()));
    }

    @Test
    public void testCheckStatusIncomingRequest() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(current.getUsername())).thenReturn(current);

        current.getPending().add(other.getUsername());
        
        assertTrue(!current.getConnections().contains(other.getUsername()));
        assertTrue(!current.getSent().contains(other.getUsername()));
        assertTrue(current.getPending().contains(other.getUsername()));
        assertEquals(3, dao.checkStatus(current.getUsername(), other.getUsername()));
    }

    @Test
    public void testCheckStatusUserNotFound() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(current.getUsername()))
            .thenThrow(UserNotFoundException.class);
        assertThrows(UserNotFoundException.class,
            () -> dao.checkStatus(current.getUsername(), other.getUsername()));
    }
}
