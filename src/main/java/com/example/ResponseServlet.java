package com.example;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/response")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2 MB
    maxFileSize = 1024 * 1024 * 10,       // 10 MB
    maxRequestSize = 1024 * 1024 * 50     // 50 MB
)
public class ResponseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L; 
    private static final String SAVE_DIR = "uploads";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Form verilerini al
        String name = request.getParameter("name");
        String email = request.getParameter("email");

        // Eksik alan kontrolü
        if (name == null || name.isEmpty() || email == null || email.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
            out.println("<h2>Hata: Tüm alanlar doldurulmalıdır!</h2>");
            return;
        }

        // Dosya yükleme işlemi
        String appPath = request.getServletContext().getRealPath("");
        String savePath = appPath + File.separator + SAVE_DIR;

        File fileSaveDir = new File(savePath);
        if (!fileSaveDir.exists()) {
            fileSaveDir.mkdir();
        }

        Part part = request.getPart("cv"); // Dosya parçasını al
        String fileName = extractFileName(part); // Dosya adını çıkar

        // Dosya yükleme başarılı mı?
        boolean isFileUploadSuccess = false;
        try {
            part.write(savePath + File.separator + fileName); // Dosyayı kaydet
            isFileUploadSuccess = true;
        } catch (IOException e) {
            isFileUploadSuccess = false;
        }

        // HTTP durum kodunu ayarla
        if (isFileUploadSuccess) {
            response.setStatus(HttpServletResponse.SC_OK); // 200 OK
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }

        // Response header'ları ekle
        response.setHeader("Server", "MyServlet/1.0");
        response.setHeader("Date", new Date().toString());
        response.setHeader("Content-Language", "tr-TR");

        // Kullanıcıya bilgileri göster
        out.println("<h2>Form Verileri:</h2>");
        out.println("Ad Soyad: " + name + "<br>");
        out.println("E-posta: " + email + "<br>");
        out.println("<h2>Dosya Bilgileri:</h2>");
        out.println("Dosya Adı: " + fileName + "<br>");
        out.println("Dosya Yolu: " + savePath + File.separator + fileName + "<br>");
        out.println("<h2>HTTP Durum Kodu:</h2>");
        out.println("Durum Kodu: " + response.getStatus() + "<br>");
        out.println("<h2>Response Header'ları:</h2>");
        out.println("Server: " + response.getHeader("Server") + "<br>");
        out.println("Date: " + response.getHeader("Date") + "<br>");
        out.println("Content-Language: " + response.getHeader("Content-Language") + "<br>");
    }

    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length() - 1);
            }
        }
        return "";
    }
}