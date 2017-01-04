package image;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.media.jai.remote.SerializableRenderedImage;

public class SeriImage extends Image implements Serializable{
	protected transient RenderedImage image;
	
	public SeriImage(RenderedImage image){
		//super(imageType, imageType, imageType, null);
		this.image=image;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.defaultWriteObject();
		out.writeObject(new SerializableRenderedImage(image,true));
	}
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException{
		in.defaultReadObject();
		image=(RenderedImage)in.readObject();
	}

	@Override
	public int getWidth(ImageObserver observer) {
		// TODO Auto-generated method stub
		return image.getWidth();
	}

	@Override
	public int getHeight(ImageObserver observer) {
		// TODO Auto-generated method stub
		return image.getHeight();
	}

	@Override
	public ImageProducer getSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Graphics getGraphics() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getProperty(String name, ImageObserver observer) {
		// TODO Auto-generated method stub
		return null;
	}
}