package com.example.qrcodearticleapp.controller;

import com.example.qrcodearticleapp.entity.Article;
import com.example.qrcodearticleapp.entity.Entrepot;
import com.example.qrcodearticleapp.service.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/articles")
public class ArticleController {

    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    private final ArticleService articleService;
    private final EntrepotService entrepotService;
    private final FabricantService fabricantService;
    private final FournisseurService fournisseurService;
    private final QRCodeService qrCodeService;

    @Autowired
    public ArticleController(ArticleService articleService, EntrepotService entrepotService,
                             FabricantService fabricantService, FournisseurService fournisseurService,
                             QRCodeService qrCodeService) {
        this.articleService = articleService;
        this.entrepotService = entrepotService;
        this.fabricantService = fabricantService;
        this.fournisseurService = fournisseurService;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping
    public String getAllArticles(Model model) {
        List<Article> articles = articleService.getAllArticles();
        model.addAttribute("articles", articles);
        return "articles";
    }

    @GetMapping("/{id}")
    public String getArticleById(@PathVariable Long id, Model model, HttpSession session) {
        Article article = articleService.getArticleById(id);
        if (article.getCodeQr() != null) {
            String encodedQrCode = Base64Utils.encodeToString(article.getCodeQr());
            model.addAttribute("encodedQrCode", encodedQrCode);
        }

        @SuppressWarnings("unchecked")
        List<String> includeFields = (List<String>) session.getAttribute("includeFields_" + id);
        if (includeFields == null) {
            includeFields = new ArrayList<>();
        }

        model.addAttribute("includeFields", includeFields);
        model.addAttribute("article", article);
        return "article";
    }

    @GetMapping("/new")
    public String createArticleForm(Model model) {
        model.addAttribute("article", new Article());
        model.addAttribute("entrepots", entrepotService.getAllEntrepots());
        model.addAttribute("fabricants", fabricantService.getAllFabricants());
        model.addAttribute("fournisseurs", fournisseurService.getAllFournisseurs());
        model.addAttribute("includeFields", new ArrayList<String>()); // Initialize includeFields
        return "article-form";
    }

    @PostMapping
    public String saveArticle(@ModelAttribute Article article, @RequestParam(value = "includeFields", required = false) List<String> includeFields, HttpSession session) {
        if (includeFields == null) {
            includeFields = new ArrayList<>();
        }

        if (article.getEntrepot() != null && article.getEntrepot().getId() != null) {
            Entrepot entrepot = entrepotService.getEntrepotById(article.getEntrepot().getId());
            article.setEntrepot(entrepot);
        }

        String qrContent = generateQRContent(article, includeFields);

        article.setCodeQr(generateQRCode(qrContent));
        articleService.saveArticle(article);

        // Save the includeFields in session
        session.setAttribute("includeFields_" + article.getSerialNumber(), includeFields);

        return "redirect:/articles";
    }

    @GetMapping("/edit/{id}")
    public String editArticleForm(@PathVariable Long id, Model model, HttpSession session) {
        Article article = articleService.getArticleById(id);
        model.addAttribute("article", article);
        model.addAttribute("entrepots", entrepotService.getAllEntrepots());
        model.addAttribute("fabricants", fabricantService.getAllFabricants());
        model.addAttribute("fournisseurs", fournisseurService.getAllFournisseurs());

        @SuppressWarnings("unchecked")
        List<String> includeFields = (List<String>) session.getAttribute("includeFields_" + id);
        if (includeFields == null) {
            includeFields = new ArrayList<>();
        }
        model.addAttribute("includeFields", includeFields);

        return "article-form";
    }

    @PostMapping("/{id}")
    public String updateArticle(@PathVariable Long id, @ModelAttribute Article article, @RequestParam(value = "includeFields", required = false) List<String> includeFields, HttpSession session) {
        if (includeFields == null) {
            includeFields = new ArrayList<>();
        }

        article.setSerialNumber(id);
        if (article.getEntrepot() != null && article.getEntrepot().getId() != null) {
            Entrepot entrepot = entrepotService.getEntrepotById(article.getEntrepot().getId());
            article.setEntrepot(entrepot);
        }

        String qrContent = generateQRContent(article, includeFields);

        article.setCodeQr(generateQRCode(qrContent));
        articleService.saveArticle(article);

        // Save the includeFields in session
        session.setAttribute("includeFields_" + id, includeFields);

        return "redirect:/articles";
    }

    private String generateQRContent(Article article, List<String> includeFields) {
        StringBuilder qrContent = new StringBuilder();
        qrContent.append("Serial Number: ").append(article.getSerialNumber()).append(", ");
        if (includeFields.contains("nom")) {
            qrContent.append("Name: ").append(article.getNom()).append(", ");
        }
        if (includeFields.contains("longueur")) {
            qrContent.append("Length: ").append(article.getLongueur()).append(", ");
        }
        if (includeFields.contains("largeur")) {
            qrContent.append("Width: ").append(article.getLargeur()).append(", ");
        }
        if (includeFields.contains("hauteur")) {
            qrContent.append("Height: ").append(article.getHauteur()).append(", ");
        }
        if (includeFields.contains("categorie")) {
            qrContent.append("Category: ").append(article.getCategorie()).append(", ");
        }
        if (article.getEntrepot() != null && includeFields.contains("entrepot")) {
            qrContent.append("Warehouse: ").append(article.getEntrepot().getNom()).append(", ");
        }
        if (article.getFabricant() != null && includeFields.contains("fabricant")) {
            qrContent.append("Manufacturer: ").append(article.getFabricant().getName()).append(", ");
        }
        if (article.getFournisseur() != null && includeFields.contains("fournisseur")) {
            qrContent.append("Supplier: ").append(article.getFournisseur().getName()).append(", ");
        }
        // Remove the last comma and space
        if (qrContent.length() > 2) {
            qrContent.setLength(qrContent.length() - 2);
        }
        return qrContent.toString();
    }

    private byte[] generateQRCode(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", byteArrayOutputStream);

            return byteArrayOutputStream.toByteArray();
        } catch (WriterException | IOException e) {
            logger.error("Error generating QR code", e);
            return null;
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return "redirect:/articles";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> downloadQRCode(@PathVariable Long id) throws IOException {
        Article article = articleService.getArticleById(id);
        if (article != null && article.getCodeQr() != null) {
            Path path = Paths.get(System.getProperty("user.home"), "Desktop", "Codes");
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            Path filePath = path.resolve("QR_Code_" + id + ".png");
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(article.getCodeQr());
            }

            ByteArrayInputStream bis = new ByteArrayInputStream(article.getCodeQr());
            InputStreamResource resource = new InputStreamResource(bis);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filePath.getFileName().toString())
                    .contentType(MediaType.IMAGE_PNG)
                    .contentLength(article.getCodeQr().length)
                    .body(resource);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/upload")
    public String showUploadForm() {
        return "upload-form";
    }

    @PostMapping("/upload")
    public String uploadArticle(@RequestParam("file") MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            Path path = Paths.get(System.getProperty("user.home"), "Desktop", "Codes", file.getOriginalFilename());
            byte[] qrCodeData = Files.readAllBytes(path);
            Article article = qrCodeService.decodeQRCode(qrCodeData);
            articleService.saveArticle(article);
        }
        return "redirect:/articles";
    }
}
