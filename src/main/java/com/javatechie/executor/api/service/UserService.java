package com.javatechie.executor.api.service;

import com.javatechie.executor.api.entity.User;
import com.javatechie.executor.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    Object target;
    Logger logger = LoggerFactory.getLogger(UserService.class);

    @Async
    public CompletableFuture<List<User>> saveUsers(MultipartFile file) throws Exception {
        long start = System.currentTimeMillis();
        List<User> users = parseCSVFile(file);
        logger.info("saving list of users of size {}", users.size(), "" + Thread.currentThread().getName());
        users = repository.saveAll(users);
        long end = System.currentTimeMillis();
        logger.info("Total time {}", (end - start));
        
        
        
        StringBuffer builder = new StringBuffer();
        builder.append("hola boludo ");
        List<CompletableFuture<String>> listCost = IntStream.range(0,1000)
											        		.mapToObj(String::valueOf)
											        		.map(this::produceFuture)
											        		.sequential()
											        		.map((future) -> future.whenComplete((value, throwable)-> builder.append(value)))
											        		.collect(Collectors.toList());
        
        
        listCost.forEach((future)-> { 
        	
        	String a = String.format("%s - %s ,Thread: %s\n", future.toString(), "futuro", Thread.currentThread().getName()); 
        	System.out.println(a);
        	System.out.println(future.join()); 
        
        });
        
        
        
        return CompletableFuture.completedFuture(users);
    }
    
    
    private CompletableFuture<String> produceFuture(String value){
    	return CompletableFuture.supplyAsync(()-> String.format("%s - %s ,Thread: %s\n", value, "hello", Thread.currentThread().getName()));
    }
    
    @Async
    public CompletableFuture<List<User>> findAllUsers(){
        logger.info("get list of user by "+Thread.currentThread().getName());
        List<User> users=repository.findAll();
        return CompletableFuture.completedFuture(users);
    }

    private List<User> parseCSVFile(final MultipartFile file) throws Exception {
        final List<User> users = new ArrayList<>();
        try {
            try (final BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    final String[] data = line.split(",");
                    final User user = new User();
                    user.setName(data[0]);
                    user.setEmail(data[1]);
                    user.setGender(data[2]);
                    users.add(user);
                }
                return users;
            }
        } catch (final IOException e) {
            logger.error("Failed to parse CSV file {}", e);
            throw new Exception("Failed to parse CSV file {}", e);
        }
    }
}
