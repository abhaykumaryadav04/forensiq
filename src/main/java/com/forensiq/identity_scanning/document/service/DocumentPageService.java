package com.forensiq.identity_scanning.document.service;

import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.forensiq.identity_scanning.document.entity.Document;
import com.forensiq.identity_scanning.document.entity.DocumentPage;
import com.forensiq.identity_scanning.document.entity.PageStatus;
import com.forensiq.identity_scanning.document.repo.DocumentPageRepo;

@Service
public class DocumentPageService {
@Autowired
private DocumentPageRepo documentPageRepo;


public void createImage(Document document) throws IOException{
    if("application/pdf".equals(document.getMimeType())){
        createPdfPages(document);
    }else{
      createImagePage(document);
    }
}

private void createImagePage(Document document) throws IOException {
 Path path=Path.of(document.getStoragePath());
    BufferedImage image = javax.imageio.ImageIO.read( path.toFile() );

        if (image == null) {
           throw new IOException("Unable to read image" );
        }
        DocumentPage page = createPage( document,1,  image );
        page.setImagepath(path.toString());
        page.setStatus(PageStatus.IMAGE_CREATED );
       documentPageRepo.save(page);
}

private DocumentPage createPage(Document document, int pageNumber, BufferedImage image) {
    DocumentPage page=DocumentPage.builder().document(document)
                                  .dpi(300)
                                  .height(image.getHeight())
                                  .pageNumber(pageNumber)
                                  .status(PageStatus.CREATED)
                                  .width(image.getWidth())
                                  .build();
            
    return page;
}

private void createPdfPages(Document document) throws IOException {

    Path pdfPath = Path.of(document.getStoragePath());

    Path outputDirectory = pdfPath.getParent()
            .resolve("pages");

    java.nio.file.Files.createDirectories(outputDirectory);

    try (PDDocument pdDocument = Loader.loadPDF(pdfPath.toFile())) {
        PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
        int totalPages = pdDocument.getNumberOfPages();
        for (int i= 0; i < totalPages; i++) {
            int pageNumber = i + 1;
            BufferedImage image = pdfRenderer.renderImageWithDPI(
                            i,
                            300,
                            ImageType.RGB
                    );
            Path imagePath = outputDirectory.resolve(
                    "document_"
                    + document.getId()
                    + "_page_"
                    + pageNumber
                    + ".png"
            );
            ImageIO.write(image, "png",imagePath.toFile() );
            DocumentPage documentPage =createPage(document,pageNumber,image );
            documentPage.setImagepath(imagePath.toString());
            documentPage.setStatus(PageStatus.IMAGE_CREATED );
            documentPageRepo.save(documentPage);
        }
    }
}
}
