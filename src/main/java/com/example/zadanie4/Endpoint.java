package com.example.zadanie4;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class Endpoint {

    private BufferedWriter bw;

    @GetMapping("/")
    public String main(Model model) {
        Input input = new Input();
        input.setName("");
        model.addAttribute("input", input);
        return "index";
    }


    @RequestMapping(value = "/result", method = RequestMethod.POST)
    public String search(@ModelAttribute Input input, Model model) throws IOException {
        model.addAttribute("searchName", input.name);

        List<String> names = new ArrayList<>();
        String url = buildUrl(input);
        Document doc = Jsoup.connect(url).validateTLSCertificates(false).get();
        Elements elements = doc.select("div.user-info");

        for (Element element : elements) {
            names.add(element.select("a").attr("title"));
        }

        model.addAttribute("names", names);
        model.addAttribute("input", input);

        return "result";
    }


    @RequestMapping(value = "/card/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable String id, Model model) {
        String[] data = id.split(" ");
        writeFile(data);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=card.vcf");
        Resource fileSystemResource = new FileSystemResource("card.vcf");
        return ResponseEntity.ok()
                .headers(headers)
                .body(fileSystemResource);
    }

    private void writeFile(String[] data) {
        StringBuilder builder = new StringBuilder();
        builder.append("card");
        builder.append(".vcf");

        try {
            File file = new File(builder.toString());
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);
            bw.write("BEGIN:VCARD" + System.lineSeparator());
            bw.write("VERSION:2.1" + System.lineSeparator());
            bw.write("FN:" + data[0] + " " + data[1] + System.lineSeparator());
            bw.write("N:" + data[0] + ";" + data[1] + System.lineSeparator());
            bw.write("TEL;CELL:+48-602-602-602" + System.lineSeparator());
            bw.write("END:VCARD");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buildUrl(@ModelAttribute("foo") Input input) {
        String baseURL = "http://adm.edu.p.lodz.pl/user/users.php?search=";
        String[] data = input.name.split(" ");
        StringBuilder builder = new StringBuilder("");
        for (int i = 0; i < data.length; i++) {
            builder.append(data[i]);
            builder.append("+");
        }
        return baseURL + builder.toString();
    }


    private class Input {
        String name;

        public Input() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
