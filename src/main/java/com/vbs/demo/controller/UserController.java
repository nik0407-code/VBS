package com.vbs.demo.controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.LoginDto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.Transaction;
import com.vbs.demo.models.Updates;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UpdatesRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")

public class UserController {

    @Autowired
    UpdatesRepo updatesRepo;

    @Autowired
    HistoryRepo historyRepo;

    @Autowired
    TransactionRepo transactionRepo;

    @Autowired
    UserRepo userRepo ;
    @PostMapping("/register")
    public String register(@RequestBody User user)
    {
        userRepo.save(user);
        return "signup successful";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginDto u)
    {
        User user= userRepo.findByUsername(u.getUsername());
                if(user==null)
                {
                    return"Username not found";
                }
                if(!u.getPassword().equals(user.getPassword()))
                {
                    return"password incorrect";
                }
                if(!u.getRole().equals(user.getRole()))
                {
                    return"incorrect Role";
                }
                return String.valueOf(user.getId());
    }
    @GetMapping("/get-details/{id}")
    public DisplayDto display(@PathVariable int id)
    {
        User user=userRepo.findById(id).orElseThrow(()->new RuntimeException("User not found"));
        DisplayDto displayDto =new DisplayDto();
        displayDto.setUsername(user.getUsername());
        displayDto.setBalance(user.getBalance());
        return displayDto;
    }

    @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj)
    {
        User user =userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("not found"));
        Updates h1 = new Updates();
        if(obj.getKey().equalsIgnoreCase("name"))
        {
            if(obj.getValue().equals((user.getName())))return"cannot be same";
            h1.setDescription("name changed from "+user.getName()+" to "+obj.getValue());
            h1.setUserId(obj.getId());
            user.setName(obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("password"))
        {
            if(obj.getValue().equals((user.getPassword())))return"cannot be same";
            h1.setDescription("Password changed from "+user.getPassword()+" to "+obj.getValue());
            h1.setUserId(obj.getId());
            user.setPassword(obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("email"))
        {
            if(obj.getValue().equals((user.getEmail())))return"cannot be same";
            User user2=userRepo.findByEmail(obj.getValue());
            if(user2!=null)return "email already exists";
            h1.setDescription("email changed from "+user.getEmail()+" to "+obj.getValue());
            h1.setUserId(obj.getId());
            user.setEmail(obj.getValue());
        }
        else
        {
            return "invalid key";
        }
        updatesRepo.save(h1);
        userRepo.save(user);
        return "update successfully";
    }

    @PostMapping("/add/{adminId}")
    public String add(@RequestBody User user,@PathVariable int adminId)
    {
        History h1 = new History();
        h1.setDescription("Admin "+adminId+" Created user "+user.getUsername());
        historyRepo.save(h1);
        userRepo.save(user);
        if(user.getBalance()>0)
        {
          User user2 = userRepo.findByUsername(user.getUsername());
            Transaction t = new Transaction();
            t.setAmount(user.getBalance());
            t.setCurrBalance(user.getBalance());
            t.setDescription("Rs "+ user.getBalance() +" deposit successful");
            t.setUserId(user2.getId());
            transactionRepo.save(t);
        }
        return " Added successfully";
    }

    @DeleteMapping("delete-user/{userId}/admin/{adminId}")
    public String delete (@PathVariable int userId,@PathVariable int adminId)
    {
        User user = userRepo.findById(userId)
                .orElseThrow(()->new RuntimeException("Not found"));
        if(user.getBalance()>0)
        {
            return "Balance Should be zero";
        }
        History h1 = new History();
        h1.setDescription("Admin"+adminId+"Deleted User "+user.getUsername());
        historyRepo.save(h1);
        userRepo.delete(user);
        return"User Deleted Successfully";
    }

    @GetMapping("/users")
    public List<User> getAllUsers(@RequestParam  String sortBy, @RequestParam String order)
    {
        Sort sort;
        if(order.equalsIgnoreCase("desc"))
        {
            sort = Sort.by(sortBy).descending();
        }
        else
        {
            sort = Sort.by(sortBy).ascending();
        }
        return userRepo.findAllByRole("customer",sort);
    }

    @GetMapping("/users/{keyword}")
    public List<User> getUser(@PathVariable String keyword)
    {
        return userRepo.findByUsernameContainingIgnoreCaseAndRole(keyword,"customer");
    }


}
