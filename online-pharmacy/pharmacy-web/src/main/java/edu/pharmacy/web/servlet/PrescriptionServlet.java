package edu.pharmacy.web.servlet;

import edu.pharmacy.dao.PrescriptionDAO;
import edu.pharmacy.web.bean.CartBean;
import edu.pharmacy.web.bean.UserBean;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;

@WebServlet("/prescription")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024)
public class PrescriptionServlet extends HttpServlet {
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserBean user = (UserBean) request.getSession().getAttribute("user");
        CartBean cart = (CartBean) request.getSession().getAttribute("cart");
        Path savedFile = null;
        try {
            long medicineId = positiveLong(request.getParameter("medicineId"));
            if (cart == null || cart.getItems().stream()
                    .noneMatch(item -> item.getMedicineId() == medicineId)) {
                throw new IllegalArgumentException("Add the medicine to your cart first.");
            }
            Part part = request.getPart("prescription");
            String extension = allowedExtension(part);
            verifyFileSignature(part, extension);

            Path uploadDirectory = uploadDirectory();
            Files.createDirectories(uploadDirectory);
            String fileName = UUID.randomUUID() + extension;
            savedFile = uploadDirectory.resolve(fileName);
            try (InputStream input = part.getInputStream()) {
                Files.copy(input, savedFile);
            }

            long prescriptionId = prescriptionDAO.createPending(user.getId(), medicineId, fileName);
            cart.setPrescriptionId(medicineId, prescriptionId);
            request.getSession().setAttribute("flashError",
                    "Prescription uploaded. A pharmacist must approve it before checkout.");
            response.sendRedirect(request.getContextPath() + "/cart");
        } catch (IllegalArgumentException exception) {
            request.getSession().setAttribute("flashError", exception.getMessage());
            response.sendRedirect(request.getContextPath() + "/cart");
        } catch (IllegalStateException exception) {
            request.getSession().setAttribute("flashError",
                    "The prescription file exceeded the 5 MB upload limit.");
            response.sendRedirect(request.getContextPath() + "/cart");
        } catch (SQLException | IOException exception) {
            removeSavedFile(savedFile, exception);
            getServletContext().log("Prescription upload failed.", exception);
            request.getSession().setAttribute("flashError",
                    "The prescription could not be saved right now.");
            response.sendRedirect(request.getContextPath() + "/cart");
        }
    }

    private static String allowedExtension(Part part) {
        String submittedName = part.getSubmittedFileName();
        if (part.getSize() <= 0 || part.getSize() > 5L * 1024 * 1024
                || submittedName == null) {
            throw new IllegalArgumentException("Select a prescription file up to 5 MB.");
        }
        String lowerName = submittedName.toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".pdf")) {
            return ".pdf";
        }
        if (lowerName.endsWith(".png")) {
            return ".png";
        }
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return ".jpg";
        }
        throw new IllegalArgumentException("Upload a PDF, PNG, or JPEG prescription.");
    }

    private static void verifyFileSignature(Part part, String extension) throws IOException {
        byte[] signature;
        try (InputStream input = part.getInputStream()) {
            signature = input.readNBytes(8);
        }
        boolean valid = switch (extension) {
            case ".pdf" -> signature.length >= 5
                    && signature[0] == '%' && signature[1] == 'P'
                    && signature[2] == 'D' && signature[3] == 'F' && signature[4] == '-';
            case ".png" -> signature.length >= 8
                    && signature[0] == (byte) 0x89 && signature[1] == 'P'
                    && signature[2] == 'N' && signature[3] == 'G'
                    && signature[4] == 13 && signature[5] == 10
                    && signature[6] == 26 && signature[7] == 10;
            case ".jpg" -> signature.length >= 3
                    && signature[0] == (byte) 0xFF && signature[1] == (byte) 0xD8
                    && signature[2] == (byte) 0xFF;
            default -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException("The selected file is not a valid PDF or image.");
        }
    }

    private Path uploadDirectory() {
        String configured = System.getProperty("pharmacy.upload.dir");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("PHARMACY_UPLOAD_DIR");
        }
        if (configured == null || configured.isBlank()) {
            configured = Path.of(System.getProperty("java.io.tmpdir"),
                    "online-pharmacy-prescriptions").toString();
        }
        return Path.of(configured).toAbsolutePath().normalize();
    }

    private static long positiveLong(String value) {
        long parsed = Long.parseLong(value);
        if (parsed <= 0) {
            throw new IllegalArgumentException("Medicine id must be greater than zero.");
        }
        return parsed;
    }

    private void removeSavedFile(Path file, Exception original) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException cleanupFailure) {
            original.addSuppressed(cleanupFailure);
            getServletContext().log("Could not remove incomplete prescription upload.", cleanupFailure);
        }
    }
}
