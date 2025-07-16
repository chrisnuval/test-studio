package com.recorder.service;

import com.recorder.model.BrowserAction;
import com.recorder.util.LocatorHealer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class LocatorHealerService {

    @Inject
    LocatorHealer locatorHealer;

    public Optional<WebElement> heal(WebDriver driver, BrowserAction action) {
        String healedSelector = locatorHealer.tryHealing(action, driver);
        if (healedSelector == null) return Optional.empty();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(healedSelector)));

            log.info("Element previous selector: " + action.getSelector());
            log.info("Element Healed Selector: " + healedSelector);

            //update selector
            action.setSelector(healedSelector);
            action.setFindBy("cssSelector");

            return Optional.of(el);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
