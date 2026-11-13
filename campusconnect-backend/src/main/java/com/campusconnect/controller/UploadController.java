package com.campusconnect.controller;

import com.campusconnect.entity.*;
import com.campusconnect.enums.MediaType;
import com.campusconnect.exception.BadRequestException;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.*;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB, matches application.properties

    private final Cloudinary cloudinary;
    private final EventRepository eventRepository;
    private final EventMediaRepository eventMediaRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;

    @PostMapping("/event/{id}/images")
    public ResponseEntity<?> uploadEventImages(@PathVariable Long id, @RequestParam("files") MultipartFile[] files) throws Exception {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        validateFiles(files);

        int order = eventMediaRepository.findByEvent_IdOrderByDisplayOrderAsc(id).size();

        for (MultipartFile file : files) {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String imageUrl = uploadResult.get("secure_url").toString();

            EventMedia media = new EventMedia();
            media.setImageUrl(imageUrl);
            media.setMediaType(MediaType.IMAGE);
            media.setDisplayOrder(order++);
            media.setEvent(event);
            eventMediaRepository.save(media);
        }

        return ResponseEntity.ok(event);
    }

    @PostMapping("/user/{id}/profile")
    public ResponseEntity<?> uploadProfileImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        validateFiles(new MultipartFile[]{file});

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        user.setProfileImage(uploadResult.get("secure_url").toString());
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/vendor/{id}/banner")
    public ResponseEntity<?> uploadVendorBanner(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws Exception {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        validateFiles(new MultipartFile[]{file});

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        vendor.setBannerImage(uploadResult.get("secure_url").toString());
        vendorRepository.save(vendor);

        return ResponseEntity.ok(vendor);
    }

    private void validateFiles(MultipartFile[] files) {
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new BadRequestException("One or more files are empty");
            }
            if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                throw new BadRequestException("File '" + file.getOriginalFilename() + "' exceeds the 10MB size limit");
            }
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
                throw new BadRequestException("File '" + file.getOriginalFilename() + "' must be a JPEG, PNG, WEBP, or GIF image");
            }
        }
    }
}