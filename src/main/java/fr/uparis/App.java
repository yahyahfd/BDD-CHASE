package fr.uparis;

import fr.uparis.database.Database;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Starting the program..." );
        Database myDb = new Database("New DB");
    }
}
