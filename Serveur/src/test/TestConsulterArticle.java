/**package Test;

import Serveur.BricoMerlinServicesImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import java.rmi.RemoteException;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestConsulterArticle {
    private BricoMerlinServicesImpl service;
    private Connection mockConnection;
    private Statement mockStatement;
    private ResultSet mockResultSet;
    private ResultSetMetaData mockMetaData;

    @BeforeEach
    void setUp() throws Exception {
        mockConnection = mock(Connection.class);
        mockStatement = mock(Statement.class);
        mockResultSet = mock(ResultSet.class);
        mockMetaData = mock(ResultSetMetaData.class);

        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(4);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString(1)).thenReturn("123");
        when(mockResultSet.getString(2)).thenReturn("Bricolage");
        when(mockResultSet.getString(3)).thenReturn("15.99");
        when(mockResultSet.getString(4)).thenReturn("50");

        service = new BricoMerlinServicesImpl();
        service.con = mockConnection; // Injection de la connexion mockée
    }

    @Test
    void testConsulterArticle() throws RemoteException, SQLException {
        String[] expected = {"123", "Bricolage", "15.99", "50"};
        String[] result = service.ConsulterArticle("123");

        assertArrayEquals(expected, result);
        verify(mockStatement, times(1)).executeQuery(anyString());
        verify(mockResultSet, times(1)).next();
    }
}
*/