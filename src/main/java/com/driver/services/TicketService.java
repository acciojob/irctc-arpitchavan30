package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception {

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
        //And the end return the ticketId that has come from db

        Optional<Train> trainOptional = trainRepository.findById(bookTicketEntryDto.getTrainId());

        if (trainOptional.isPresent()) {
            Train train = trainOptional.get();
            List<Ticket> bookedTickets = train.getBookedTickets();
            int totalSeats = train.getNoOfSeats();
            int bookedSeats = 0;

            Station fromStation = bookTicketEntryDto.getFromStation();
            Station toStation = bookTicketEntryDto.getToStation();

            for (Ticket ticket : bookedTickets) {
                Station ticketFromStation = ticket.getFromStation();
                Station ticketToStation = ticket.getToStation();

                // Check if the ticket's from station is before the specified toStation
                if (ticketFromStation.equals(fromStation)) {
                    // Check if the ticket's to station is after the specified fromStation
                    if (ticketToStation.equals(toStation)) {
                        // This ticket overlaps with the specified stations, so seats are booked
                        bookedSeats += ticket.getPassengersList().size();
                    }
                }
            }


            int totalAvailable = totalSeats - bookedSeats;

            if (totalAvailable < bookTicketEntryDto.getNoOfSeats()) {
                throw new Exception("Less tickets are available");
            }
            // calculate no of station to travel
            Train train1 = trainOptional.get();
            String route = train1.getRoute();
            String[] stations = train.getRoute().split(",");
            int i = 0;
            int j = 0;
            Station fromStation1 = bookTicketEntryDto.getFromStation();
            Station toStation1 = bookTicketEntryDto.getToStation();

            for (int k = 0; k < stations.length; k++) {
                String st = stations[k];
                if (st.equals(fromStation1.toString())) {
                    i = k;
                }
                if (st.equals(toStation1.toString())) {
                    j = k;
                }
            }
            if (i == 0 || j == 0) {
                throw new Exception("Invalid stations");
            }

            int totalStations = j - i;
            int pricePerPassanger = totalStations * 300;
            int Totalprice = pricePerPassanger * (bookTicketEntryDto.getNoOfSeats());

            // prepare ticket and save
            Ticket ticket = new Ticket();
            ticket.setFromStation(bookTicketEntryDto.getFromStation());
            ticket.setToStation(bookTicketEntryDto.getToStation());
            ticket.setTotalFare(Totalprice);

            Ticket savedTickets = ticketRepository.save(ticket);
            ticket.setTrain(train1);
            train1.getBookedTickets().add(savedTickets);
            Train savedTrain = trainRepository.save(train1);

            Optional<Passenger> passengerOptional = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId());
            Passenger passenger = passengerOptional.get();
            passenger.getBookedTickets().add(savedTickets);

            return savedTickets.getTicketId();
        }
        return 0;
    }


}
