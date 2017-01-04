package image;

import java.awt.Image;
import java.util.List;

public interface ImageFeatureExtractor {

    /**
     * Analyzes the given image and extracts a set of features describing the image
     * @param img (not null)
     * @return a List of identified feautures
     */
    public List<ImageFeature> getFeatures(Image img);
}