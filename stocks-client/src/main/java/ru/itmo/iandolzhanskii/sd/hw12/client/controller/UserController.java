package ru.itmo.iandolzhanskii.sd.hw12.client.controller;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.iandolzhanskii.sd.hw12.client.entity.dto.UserViewDto;
import ru.itmo.iandolzhanskii.sd.hw12.client.service.UserService;

@RestController
@RequestMapping(path = "user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserViewDto createUser(@RequestParam String userName) {
        return userService.createUser(userName);
    }

    @GetMapping(path = "{userId}")
    public UserViewDto getUser(@PathVariable Long userId) {
        return userService.getUser(userId);
    }

    @PutMapping(path = "{userId}/balance")
    public UserViewDto updateUserBalance(@PathVariable Long userId, @RequestParam BigDecimal balanceUsdDelta) {
        return userService.updateBalance(userId, balanceUsdDelta);
    }

    @PostMapping(path = "{userId}/stocks/buy")
    public UserViewDto buyStocks(
        @PathVariable Long userId,
        @RequestParam String symbol,
        @RequestParam Long amount
    ) {
        return userService.buyStocks(userId, symbol, amount);
    }

    @PostMapping(path = "{userId}/stocks/sell")
    public UserViewDto sellStocks(
        @PathVariable Long userId,
        @RequestParam String symbol,
        @RequestParam Long amount
    ) {
        return userService.sellStocks(userId, symbol, amount);
    }
}
