package com.example.demo_extract;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.*;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicReference;

public class ImageViewer extends Application {

    private Point2D dragAnchor;
    private Rectangle rectangle = null;
    private BufferedImage pdfImage;
    public static com.itextpdf.text.Rectangle createRectangle(
            AtomicReference<Float> x1Ref,
            AtomicReference<Float> y1Ref,
            AtomicReference<Float> x2Ref,
            AtomicReference<Float> y2Ref) {


        float x1 = x1Ref.get();
        float y1 = y1Ref.get();
        float x2 = x2Ref.get();
        float y2 = y2Ref.get();


        float llx = Math.min(x1, x2);
        float lly = Math.min(y1, y2);
        float urx = Math.max(x1, x2);
        float ury = Math.max(y1, y2);


        com.itextpdf.text.Rectangle rectangle = new com.itextpdf.text.Rectangle(llx, lly, urx, ury);

        return rectangle;
    }
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("PDF Viewer");

        try {
            pdfImage = PdfToImageConverter.convertFirstPageToImage("src/main/resources/230504_IFU_MAL_FLXT_FR_Ed1.0_2023-05-04_compressed.pdf");
        } catch (IOException e) {
            System.err.println("Failed to load or convert the PDF file.");
            e.printStackTrace();
            return;
        }

        if (pdfImage == null) {
            System.err.println("The converted image is null.");
            return;
        }

        Image fxImage = SwingFXUtils.toFXImage(pdfImage, null);
        ImageView imageView = new ImageView(fxImage);
        imageView.setPreserveRatio(true);
        Pane imagePane = new Pane(imageView);
        imagePane.setPrefSize(800, 600);
        StackPane root = new StackPane();
        root.getChildren().add(imagePane);

        Scene scene = new Scene(root, 800, 600);


        imageView.fitWidthProperty().bind(scene.widthProperty());
        imageView.fitHeightProperty().bind(scene.heightProperty());
        AtomicReference<Float> x1 = new AtomicReference<>((float) 0);
        AtomicReference<Float> y1 = new AtomicReference<>((float) 0);
        AtomicReference<Float> x2 = new AtomicReference<>((float) 0);
        AtomicReference<Float> y2 = new AtomicReference<>((float) 0);


        scene.setOnMousePressed(event -> {
            if (rectangle != null) {
                root.getChildren().remove(rectangle);
            }
            x1.set((float) event.getX());
            y1.set((float) event.getY());

            dragAnchor = new Point2D(event.getX(), event.getY());
            rectangle = new Rectangle();
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setStroke(Color.BLACK);
            rectangle.setX(dragAnchor.getX());
            rectangle.setY(dragAnchor.getY());
            imagePane.getChildren().add(rectangle);
        });

        scene.setOnMouseDragged(event -> {
            double x = event.getX();
            double y = event.getY();

            rectangle.setWidth(Math.abs(x - dragAnchor.getX()));
            rectangle.setHeight(Math.abs(y - dragAnchor.getY()));

            if (x < dragAnchor.getX()) {
                rectangle.setX(x);
            }
            if (y < dragAnchor.getY()) {
                rectangle.setY(y);
            }
        });

        scene.setOnMouseReleased(event -> {
            x2.set((float) event.getX());
            y2.set((float) event.getY());

            float width = (float) rectangle.getWidth();
            float height = (float) rectangle.getHeight();


            String src = "src/main/resources/230504_IFU_MAL_FLXT_FR_Ed1.0_2023-05-04_compressed.pdf";  // Replace with your PDF file path
            PdfReader reader;
            try {
                reader = new PdfReader(src);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            com.itextpdf.text.Rectangle dim=reader.getPageSizeWithRotation(1);

            float pdf_width= dim.getWidth();
            float pdf_height=dim.getHeight();
            width=(width/800)*pdf_width;
            height=(height/600)*pdf_height;
            System.out.println("width?"+pdf_width+"height?"+pdf_height);

            x1.set((x1.get()/800) * pdf_width);

            x2.set((x2.get() / 800) * pdf_width);


            y1.set(pdf_height - (y1.get() / 600) * pdf_height);
            y2.set(pdf_height - (y2.get() / 600) * pdf_height);

            System.out.println("Rectangle coordinates in PDF coordinate system: (" +
                    x1 + ", " + y1 + ", " + x2 + ", " + y2 + ")");
            com.itextpdf.text.Rectangle box=ImageViewer.createRectangle(x1,y1,x2,y2);


            RenderFilter filter = new RegionTextRenderFilter(box);

            TextExtractionStrategy strategy = new FilteredTextRenderListener(new LocationTextExtractionStrategy(), filter);
            String text = null;

            try {

                text = PdfTextExtractor.getTextFromPage(reader, 1, strategy);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            byte[] utf8Bytes;
            try {

                utf8Bytes = text.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            String utf16Text = new String(utf8Bytes, Charset.defaultCharset());

            System.out.println(utf16Text);


        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
