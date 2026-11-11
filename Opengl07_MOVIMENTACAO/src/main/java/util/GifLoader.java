package util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

// Le todos os frames de um GIF animado.
// Cada frame do GIF guarda so o retangulo que mudou, entao aqui os frames
// sao "montados" numa tela do tamanho do GIF, respeitando a posicao e o
// metodo de descarte (disposal) de cada frame.
public class GifLoader {

	public static BufferedImage[] loadFrames(String arquivo) {
		try {
			ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
			ImageInputStream in = ImageIO.createImageInputStream(new File(arquivo));
			reader.setInput(in);

			int n = reader.getNumImages(true);
			BufferedImage[] frames = new BufferedImage[n];

			// tamanho do GIF (logical screen); se nao tiver, usa o tamanho do frame 0
			int largura = reader.getWidth(0);
			int altura = reader.getHeight(0);
			IIOMetadataNode stream = (IIOMetadataNode) reader.getStreamMetadata().getAsTree("javax_imageio_gif_stream_1.0");
			if (stream.getElementsByTagName("LogicalScreenDescriptor").getLength() > 0) {
				IIOMetadataNode lsd = (IIOMetadataNode) stream.getElementsByTagName("LogicalScreenDescriptor").item(0);
				largura = Integer.parseInt(lsd.getAttribute("logicalScreenWidth"));
				altura = Integer.parseInt(lsd.getAttribute("logicalScreenHeight"));
			}

			BufferedImage tela = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);

			for (int i = 0; i < n; i++) {
				IIOMetadataNode meta = (IIOMetadataNode) reader.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0");
				IIOMetadataNode desc = (IIOMetadataNode) meta.getElementsByTagName("ImageDescriptor").item(0);
				int fx = Integer.parseInt(desc.getAttribute("imageLeftPosition"));
				int fy = Integer.parseInt(desc.getAttribute("imageTopPosition"));

				String descarte = "none";
				if (meta.getElementsByTagName("GraphicControlExtension").getLength() > 0) {
					IIOMetadataNode gce = (IIOMetadataNode) meta.getElementsByTagName("GraphicControlExtension").item(0);
					descarte = gce.getAttribute("disposalMethod");
				}

				BufferedImage anterior = copia(tela);
				BufferedImage parte = reader.read(i);

				Graphics2D g = tela.createGraphics();
				g.drawImage(parte, fx, fy, null);
				g.dispose();

				frames[i] = copia(tela);

				// prepara a tela para o proximo frame
				if (descarte.equals("restoreToBackgroundColor")) {
					g = tela.createGraphics();
					g.setComposite(AlphaComposite.Clear);
					g.fillRect(fx, fy, parte.getWidth(), parte.getHeight());
					g.dispose();
				} else if (descarte.equals("restoreToPrevious")) {
					tela = anterior;
				}
			}

			in.close();
			reader.dispose();
			return frames;
		} catch (Exception e) {
			System.err.println("Erro ao carregar o GIF " + arquivo);
			e.printStackTrace();
			return new BufferedImage[0];
		}
	}

	private static BufferedImage copia(BufferedImage img) {
		BufferedImage c = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = c.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return c;
	}
}
