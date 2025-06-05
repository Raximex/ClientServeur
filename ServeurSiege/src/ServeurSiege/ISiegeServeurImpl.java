package ServeurSiege;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class  ISiegeServeurImpl implements ISiegeServeur{

    @Override
    public HashMap<String, Float> miseAJourPrix(String[] refArticles) throws RemoteException, SQLException {
        return null;
    }
}
