//package com.recorder;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.recorder.model.BrowserAction;
//import com.recorder.model.FeatureCleanRequest;
//import com.recorder.model.Locator;
//import com.recorder.model.OllamaRequest;
//import io.github.bonigarcia.wdm.WebDriverManager;
//import jakarta.ws.rs.*;
//import jakarta.ws.rs.core.MediaType;
//import jakarta.ws.rs.core.Response;
//import lombok.extern.slf4j.Slf4j;
//import org.openqa.selenium.*;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.openqa.selenium.support.ui.WebDriverWait;
//
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.time.Duration;
//import java.util.*;
//
//@Path("/record")
//@Produces(MediaType.APPLICATION_JSON)
//@Consumes(MediaType.APPLICATION_JSON)
//@Slf4j
//public class RecorderResource {
//
//    private static final List<BrowserAction> actions = new ArrayList<>();
//    private static final List<Locator> locators = new ArrayList<>();
//    private static String baseUrl = "https://example.com";  // default
//    private WebDriver driver = null;
//
//    public WebDriver createChromeDriverWithCorsDisabled() {
//        WebDriverManager.chromedriver().setup();
//        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--disable-web-security");
//        options.addArguments("--allow-running-insecure-content");
//        String safeProfileDir = System.getProperty("java.io.tmpdir") + "\\chrome_profile_" + System.currentTimeMillis();
//        options.addArguments("--user-data-dir=" + safeProfileDir);
//        return new ChromeDriver(options);
//    }
//
//    @POST
//    @Path("/start")
//    public Response start(Map<String, String> payload) {
//        baseUrl = payload.get("url");
//        if (baseUrl == null || baseUrl.isEmpty()) {
//            return Response.status(Response.Status.BAD_REQUEST)
//                    .entity("URL is missing").build();
//        }
//
//        actions.clear();   // Reset previous session
//        locators.clear();
//
//        driver = createChromeDriverWithCorsDisabled();
//        try {
//            driver.get(baseUrl);
//
//            String recorderScript = """
//            (function() {
//              function getBestLocator(el) {
//                if (el.id && document.querySelectorAll('#' + el.id).length === 1) {
//                  return { findBy: 'id', value: el.id };
//                }
//                if (el.name && document.querySelectorAll('[name="' + el.name + '"]').length === 1) {
//                  return { findBy: 'name', value: el.name };
//                }
//                try {
//                  const cssPath = getCssPath(el);
//                  if (document.querySelectorAll(cssPath).length === 1) {
//                    return { findBy: 'cssSelector', value: cssPath };
//                  }
//                } catch (_) {}
//
//                return { findBy: 'xpath', value: getXPath(el) };
//              }
//
//              function getCssPath(el) {
//                if (!(el instanceof Element)) return;
//                const path = [];
//                while (el.nodeType === Node.ELEMENT_NODE) {
//                  let selector = el.nodeName.toLowerCase();
//                  if (el.id) {
//                    selector += '#' + el.id;
//                    path.unshift(selector);
//                    break;
//                  } else {
//                    let sib = el, nth = 1;
//                    while ((sib = sib.previousElementSibling)) {
//                      if (sib.nodeName.toLowerCase() === selector) nth++;
//                    }
//                    selector += ":nth-of-type(" + nth + ")";
//                  }
//                  path.unshift(selector);
//                  el = el.parentNode;
//                }
//                return path.join(" > ");
//              }
//
//              function getXPath(el) {
//                if (el.id) return '//*[@id="' + el.id + '"]';
//                if (el === document.body) return '/html/body';
//                let ix = 0;
//                const siblings = el.parentNode ? el.parentNode.childNodes : [];
//                for (let i = 0; i < siblings.length; i++) {
//                  const sibling = siblings[i];
//                  if (sibling === el) return getXPath(el.parentNode) + '/' + el.tagName.toLowerCase() + '[' + (ix + 1) + ']';
//                  if (sibling.nodeType === 1 && sibling.tagName === el.tagName) ix++;
//                }
//              }
//
//              function send(action, el, value = '') {
//                const locator = getBestLocator(el);
//                const payload = {
//                  action: action,
//                  selector: locator.value,
//                  value: value,
//                  tag: el.tagName,
//                  text: el.innerText || '',
//                  name: el.getAttribute("name") || el.getAttribute("aria-label") || el.getAttribute("placeholder") || '',
//                  findBy: locator.findBy
//                };
//                fetch('http://localhost:8080/record/log', {
//                  method: 'POST',
//                  headers: { 'Content-Type': 'application/json' },
//                  body: JSON.stringify(payload)
//                });
//                if (action === 'input' || action === 'click') {
//                  fetch('http://localhost:8080/record/locator', {
//                    method: 'POST',
//                    headers: { 'Content-Type': 'application/json' },
//                    body: JSON.stringify({
//                      name: payload.name || payload.selector,
//                      lookupDetails: {
//                        findBy: payload.findBy,
//                        value: payload.selector
//                      }
//                    })
//                  });
//                }
//              }
//
//              document.addEventListener('click', function(e) {
//                send('click', e.target);
//              });
//
//              document.addEventListener('change', function(e) {
//                send('input', e.target, e.target.value);
//              });
//
//              console.log('📡 Recorder injected and running...');
//            })();
//            """;
//
//
//            ((JavascriptExecutor) driver).executeScript(recorderScript);
//            return Response.ok("Recording started at: " + baseUrl).build();
//
//        } catch (Exception e) {
//            log.error("Start error", e);
//            return Response.serverError().entity("Recording error: " + e.getMessage()).build();
//        }
//    }
//
//    @POST
//    @Path("/stop")
//    public Response stopRecording() {
//        if (driver != null) {
//            try {
//                driver.quit();
//                driver = null;
//            } catch (Exception e) {
//                log.error("Error closing browser:", e);
//            }
//        }
//        return Response.ok(new ArrayList<>(actions)).build();
//    }
//
//    @POST
//    @Path("/log")
//    public Response log(BrowserAction action) {
//        actions.add(action);
//        return Response.ok().build();
//    }
//
//    @POST
//    @Path("/locator")
//    public Response logLocator(Locator locator) {
//        locators.add(locator);
//        return Response.ok().build();
//    }
//
//    @GET
//    @Path("/locators")
//    public Response getLocators() {
//        Set<String> seen = new HashSet<>();
//        List<Map<String,     Object>> dedupedLocators = new ArrayList<>();
//
//        for (Locator loc : locators) {
//            if (loc.getName() == null || loc.getLookupDetails() == null) continue;
//
//            String name = loc.getName();
//            String findBy = loc.getLookupDetails().getFindBy();
//            String value = loc.getLookupDetails().getValue();
//            String key = name + "::" + findBy + "::" + value;
//
//            if (seen.add(key)) {
//                Map<String, Object> entry = new LinkedHashMap<>();
//                entry.put("name", name);
//
//                Map<String, String> lookup = new LinkedHashMap<>();
//                lookup.put("findBy", findBy);
//                lookup.put("value", value);
//
//                entry.put("lookupDetails", lookup);
//                dedupedLocators.add(entry);
//            }
//        }
//
//        return Response.ok(dedupedLocators).build();
//    }
//
//    @GET
//    @Path("/dump")
//    public List<BrowserAction> dump() {
//        return actions;
//    }
//
//    @POST
//    @Path("/clear")
//    public Response clear() {
//        actions.clear();
//        locators.clear();
//        return Response.ok().build();
//    }
//
//    @POST
//    @Path("/feature")
//    @Produces(MediaType.TEXT_PLAIN)
//    public Response generateFeatureFile() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("Feature: Recorded User Interaction\n");
//        sb.append("  Scenario: User interaction playback\n");
//
//        Set<String> typedKeys = new HashSet<>();
//        for (BrowserAction action : actions) {
//            String step = "";
//            String readableName = action.getSelector(); // fallback
//
//            for (Locator locator : locators) {
//                if (locator.getLookupDetails().getValue().equals(action.getSelector())) {
//                    readableName = locator.getName();
//                    break;
//                }
//            }
//
//            if ("click".equalsIgnoreCase(action.getAction())) {
//                step = "    When I click on \"" + readableName + "\"";
//            } else if ("input".equalsIgnoreCase(action.getAction())) {
//                String key = action.getSelector() + "::" + action.getValue();
//                if (typedKeys.contains(key)) continue;
//                typedKeys.add(key);
//                step = "    When I enter '" + action.getValue() + "' in '" + readableName + "'";
//            }
//            sb.append(step).append("\n");
//        }
//
//        String featureText = sb.toString();
//
//        try {
//            File featureFile = new File("recorded.feature");
//            try (FileWriter writer = new FileWriter(featureFile)) {
//                writer.write(featureText);
//            }
//        } catch (IOException e) {
//            return Response.serverError().entity("Failed to write feature file").build();
//        }
//
//        return Response.ok(featureText).build();
//    }
//
//    @POST
//    @Path("/playback")
//    public Response playback() {
//        WebDriverManager.chromedriver().setup();
//        WebDriver driver = new ChromeDriver(new ChromeOptions());
//
//        try {
//            driver.get(baseUrl);
//            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//
//            for (BrowserAction action : actions) {
//                try {
//                    By locator;
//                    switch (action.getFindBy()) {
//                        case "id":
//                            locator = By.id(action.getSelector());
//                            break;
//                        case "name":
//                            locator = By.name(action.getSelector());
//                            break;
//                        case "xpath":
//                            locator = By.xpath(action.getSelector());
//                            break;
//                        case "cssSelector":
//                        default:
//                            locator = By.cssSelector(action.getSelector());
//                            break;
//                    }
//
//                    WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
//
//                    if ("click".equalsIgnoreCase(action.getAction())) {
//                        el.click();
//                    } else if ("input".equalsIgnoreCase(action.getAction())) {
//                        el.clear();
//                        el.sendKeys(action.getValue());
//                    }
//
//                    Thread.sleep(500); // add delay for stability
//                } catch (Exception ex) {
//                    System.err.println("Error processing action: " + action + " - " + ex.getMessage());
//                }
//            }
//
//        } catch (Exception e) {
//            return Response.serverError().entity(e.getMessage()).build();
//        } finally {
//            driver.quit();
//        }
//
//        return Response.ok("Playback done").build();
//    }
//
//
//    @POST
//    @Path("/clean-feature")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.TEXT_PLAIN)
//    public Response cleanFeature(@QueryParam("provider") @DefaultValue("ollama") String provider,
//                                 @QueryParam("model") @DefaultValue("codellama") String model,
//                                 FeatureCleanRequest request) {
//        String rawFeatureText = request.getRawFeature();
//        String cleaned = rawFeatureText
//                .replaceAll("(?m)^\\s+", "")
//                .replaceAll(" {2,}", " ")
//                .trim();
//
//        try {
//            String prompt = "Clean up and improve this Cucumber feature file:\n" + cleaned;
//
//            HttpClient client = HttpClient.newHttpClient();
//            ObjectMapper mapper = new ObjectMapper();
//            String requestBody = "";
//
//            HttpRequest httpRequest;
//
//            switch (provider.toLowerCase()) {
//                case "ollama":
//                    OllamaRequest ollamaRequest = new OllamaRequest(model, prompt, false);
//                    requestBody = mapper.writeValueAsString(ollamaRequest);
//                    httpRequest = HttpRequest.newBuilder()
//                            .uri(URI.create("http://localhost:11434/api/generate"))
//                            .header("Content-Type", "application/json")
//                            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
//                            .build();
//                    break;
//
//                case "openai":
//                    // Hypothetical OpenAI call (you need to provide your key and endpoint)
//                    Map<String, Object> openaiRequest = new HashMap<>();
//                    openaiRequest.put("model", model);
//                    openaiRequest.put("messages", List.of(Map.of("role", "user", "content", prompt)));
//                    requestBody = mapper.writeValueAsString(openaiRequest);
//                    httpRequest = HttpRequest.newBuilder()
//                            .uri(URI.create("https://api.openai.com/v1/chat/completions"))
//                            .header("Authorization", "Bearer YOUR_OPENAI_API_KEY")
//                            .header("Content-Type", "application/json")
//                            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
//                            .build();
//                    break;
//
//                default:
//                    return Response.status(Response.Status.BAD_REQUEST)
//                            .entity("Unsupported AI provider: " + provider).build();
//            }
//
//            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
//            JsonNode jsonNode = mapper.readTree(response.body());
//
//            // Extract AI response
//            String cleanedFeature = switch (provider.toLowerCase()) {
//                case "ollama" -> jsonNode.get("response").asText().trim();
//                case "openai" -> jsonNode.get("choices").get(0).get("message").get("content").asText().trim();
//                default -> "";
//            };
//
//            return Response.ok(cleanedFeature).build();
//
//        } catch (Exception e) {
//            return Response.serverError().entity("Failed to connect to AI model: " + e.getMessage()).build();
//        }
//    }
//
//}
