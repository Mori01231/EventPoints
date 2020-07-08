package com.github.mori01231.eventpoints.CommandExecutors;

import com.github.mori01231.eventpoints.EventPoints;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class CreateCommandExecutor implements CommandExecutor {

    public Connection connection;
    private String host, database, username, password;
    private int port;

    private String DatabaseName;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        DatabaseName = args[0];
        r.runTaskAsynchronously(EventPoints.getInstance());

        return true;
    }

    BukkitRunnable r = new BukkitRunnable() {
        @Override
        public void run() {
            //This is where you should do your database interaction

            try {
                openConnection();
                Statement statement = connection.createStatement();

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + DatabaseName + "` (`PlayerUUID` varchar(36), `points` int)");


            } catch(ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
    };

    public void openConnection() throws SQLException, ClassNotFoundException {

        host = EventPoints.getInstance().getConfig().getString("host");
        port = EventPoints.getInstance().getConfig().getInt("port");
        database = EventPoints.getInstance().getConfig().getString("database");
        username = EventPoints.getInstance().getConfig().getString("username");
        password = EventPoints.getInstance().getConfig().getString("password");

        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
        }
    }
}
