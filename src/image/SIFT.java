package image;

import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import mpi.cbg.fly.Feature;
import mpi.cbg.fly.Filter;
import mpi.cbg.fly.FloatArray2D;
import mpi.cbg.fly.FloatArray2DSIFT;
import mpi.cbg.fly.FloatArray2DScaleOctave;

public class SIFT implements ImageFeatureExtractor
{
	// steps
	private int steps = 3;
	// initial sigma
	private float initial_sigma = 1.6f;
	// background colour
//private double bg = 0.0;
	// feature descriptor size
	private int fdsize = 4;
	// feature descriptor orientation bins
	private int fdbins = 8;
	// size restrictions for scale octaves, use octaves < max_size and > min_size only
	private int min_size = 64;
	private int max_size = 1024;
	/**
	 * Set true to double the size of the image by linear interpolation to
	 * ( with * 2 + 1 ) * ( height * 2 + 1 ).  Thus we can start identifying
	 * DoG extrema with $\sigma = INITIAL_SIGMA / 2$ like proposed by
	 * \citet{Lowe04}.
	 * 
	 * This is useful for images scmaller than 1000px per side only. 
	 */ 
	   private boolean upscale = true;
        private static float normTo1(int b) {
            return (float) (b / 255.0f);
        }
        
        private static int RGB2Grey(int argb) {
           // int a = (argb >> 24) & 0xff;
            int r = (argb >> 16) & 0xff;
            int g = (argb >> 8) & 0xff;
            int b = (argb) & 0xff;

            //int rgb=(0xff000000 | ((r<<16)&0xff0000) | ((g<<8)&0xff00) | (b&0xff));
            int y = (int) Math.round(0.299f * r + 0.587f * g + 0.114f * b);
            return y;
        }

        private FloatArray2D convert(Image img)
        {
            
            FloatArray2D image;
            PixelGrabber grabber=new PixelGrabber( img, 0, 0, -1,-1, true);
            try {
                grabber.grabPixels();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            int[] data = (int[]) grabber.getPixels();
            
            image = new FloatArray2D(grabber.getWidth(),  grabber.getHeight());
            for (int d=0;d<data.length;d++)
                        image.data[d] = normTo1(RGB2Grey(data[d]));
            return image;
        }
        
        private List<ImageFeature> convert(List<Feature> features)
        {
            List<ImageFeature> res=new ArrayList<ImageFeature>();
            for (Feature f:features)
            {
                ImageFeature imageFeature=new ImageFeature();
                imageFeature.setDescriptor(( f.descriptor));
                imageFeature.setOrientation(f.orientation);
                imageFeature.setScale(f.scale);
                res.add(imageFeature);
            }
            return res;
        }
	
//        private float[] convert(float[] desc)
//        {
//            for (int i=0;i<desc.length;i++)
//            {
//               int int_val = (int)(512 * desc[i]);
//               int_val = Math.min( 255, int_val ); 
//               dessc[i]=int_val;
//            }
//            return desc;
//        }

	public List<ImageFeature> getFeatures(Image img)
	{
		String preamb=this.getClass()+": ";
                List<Feature> fs;
			
		FloatArray2DSIFT sift = new FloatArray2DSIFT( fdsize, fdbins );		
		FloatArray2D fa = convert(img);              
		Filter.enhance( fa, 1.0f );
		
		if ( upscale )
		{
			FloatArray2D fat = new FloatArray2D( fa.width * 2 - 1, fa.height * 2 - 1 ); 
			FloatArray2DScaleOctave.upsample( fa, fat );
			fa = fat;
			fa = Filter.computeGaussianFastMirror( fa, ( float )Math.sqrt( initial_sigma * initial_sigma - 1.0 ) );
		}
		else
			fa = Filter.computeGaussianFastMirror( fa, ( float )Math.sqrt( initial_sigma * initial_sigma - 0.25 ) );
		
		long start_time = System.currentTimeMillis();
		System.out.println(preamb+"processing SIFT ..." );
		sift.init( fa, steps, initial_sigma, min_size, max_size );
		fs = sift.run( max_size );
		Collections.sort( fs );
		System.out.println(preamb+"took " + ( System.currentTimeMillis() - start_time ) + "ms" );		
		System.out.println(preamb+ fs.size() + " features identified and processed" );     
		return convert(fs);
	}

   public static void main(String args[]){
	   try {
		//BufferedImage img=ImageIO.read(new File("f:/work/maven/test.jpg"));
		//System.out.println(img.getHeight());
		WritableRenderedImage img=ImageIO.read(new File("f:/work/maven/test1.jpg"));
		System.out.println(img.getHeight());
		//有两种方式
		//1.将img强制转换为Image类型
		//2.将getFeatures(Image img)-->getFeatures(RenderedImage img)
		List<ImageFeature>imgFeatureList=new SIFT().getFeatures((Image)img);
		Iterator<ImageFeature> it=imgFeatureList.iterator();
		int num=0;
		while(it.hasNext()){
			float []desc=it.next().getDescriptor();
			for(int i=0;i<desc.length;i++)System.out.print(desc[i]+" ");
				
			System.out.println(""+num++);
		}
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   }
}