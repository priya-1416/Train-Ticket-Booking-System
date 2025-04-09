package ticket.booking.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.*;
import java.io.InputStream;


public class UserBookingService {


    private final ObjectMapper objectMapper = new ObjectMapper();

    private  User user;
    private List<User> userList;

    private final String USER_FILE_PATH = "C:\\users\\priya\\IRCTC\\app\\src\\main\\java\\ticket\\booking\\localDb\\users.json";

    //constructor
    public UserBookingService(User user)throws IOException
    {
        this.user = user;
        loadUserListFromFile();
    }

    public UserBookingService() throws IOException{
    loadUserListFromFile();
    }

    private void loadUserListFromFile()throws IOException{

        userList = objectMapper.readValue(new File(USER_FILE_PATH),new TypeReference<List<User>>(){});
    }

//    public List<User>loadUsers() throws  IOException{
//        File users = new File(User_PATH);
//        return objectMapper.readValue(users,new TypeReference<List<User>>(){});
//    }


    public Boolean loginUser(){
        Optional<User> foundUser = userList.stream().filter(user1-> {
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
            return foundUser.isPresent();
    }

    public Boolean signUp(User user1){
        try{
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        }catch(IOException ex){
            return Boolean.FALSE;
        }
    }

    private  void saveUserListToFile() throws IOException{
        File usersFile = new File(USER_FILE_PATH);
        objectMapper.writeValue(usersFile,userList);    //serialization
    }

    //json--> object(user)-->deserialization
    //object(user)-->json-->Serialization

    public void fetchBooking(){
        Optional<User> userFetched = userList.stream()
                .filter(user1 -> {
                    return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(),user1.getHashedPassword());
                }).findFirst();
        if(userFetched.isPresent()){
            userFetched.get().printTickets();
        }
    }

    public boolean cancelBooking(String ticketId) {
//        Scanner sc = new Scanner(System.in);
//        System.out.println("Enter the ticket id to cancel");
//        ticketId = sc.next();

        if (ticketId == null || ticketId.isEmpty()) {
            System.out.println("Ticket ID cannot be null or Empty.");
            return  Boolean.FALSE;
        }

       String finalTicketId1 = ticketId;   //because string is immutable

        boolean removed = user.getTicketsBooked().removeIf(ticket -> ticket.getTicketId().equals(finalTicketId1));
        String finalTicketId = ticketId;
        user.getTicketsBooked().removeIf(Ticket -> Ticket.getTicketId().equals(finalTicketId));
        if (removed) {
            System.out.println("Ticket with id" + ticketId + "Has been canceled");
            return Boolean.TRUE;
        } else {
            System.out.println("No ticket found with ID" + ticketId);
            return Boolean.FALSE;
        }
    }

    public List<Train>getTrains(String source,String destination){
        try{
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source,destination);
        }catch(IOException ex){
            //Log the exception or rethroe error it
            System.err.println("Error fetching trains :"+ex.getMessage());
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train){
        return train.getSeats();
    }

    public Boolean bookTrainSeat(Train train,int row,int seat){
        try {
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && seat >= 0 &&  seat<seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    trainService.addTrain(train);
                    return true;   //bookiing succesful
                } else {
                    return false;  //seat is already booked
                }
            }else {
                return false;     //Invalid row or seat index
            }
        }catch (IOException ex){
                return Boolean.FALSE;
        }
    }

}
