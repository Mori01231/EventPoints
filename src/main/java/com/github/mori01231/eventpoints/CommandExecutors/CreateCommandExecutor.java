package com.github.mori01231.eventpoints.CommandExecutors;

import com.github.mori01231.eventpoints.EventPoints;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;


public class CreateCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        try{
            EventPoints.getInstance().statement.executeQuery("CREATE TABLE IF NOT EXISTS '" + args[0] + "' ('PlayerUUID' varchar(36), 'points' int)");
        }catch(SQLException e){
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

        return true;
    }
}
