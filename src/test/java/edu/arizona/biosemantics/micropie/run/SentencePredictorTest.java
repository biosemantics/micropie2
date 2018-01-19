package edu.arizona.biosemantics.micropie.run;

import java.util.List;
import java.util.Set;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.micropie.CategoryPredictor;
import edu.arizona.biosemantics.micropie.Config;
import edu.arizona.biosemantics.micropie.SentencePredictor;
import edu.arizona.biosemantics.micropie.TrainSentenceClassifier;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;

public class SentencePredictorTest {
	public static void main(String[] args) {
		Config config = new Config();
		String prjInputFolder = "f:\\micropie\\micropie0.2_model";
		String prjOutputFolder ="f:\\micropie\\micropie0.2_model\\output";
		config.setInputDirectory(prjInputFolder);
		config.setOutputDirectory(prjOutputFolder);
		
		Injector injector = Guice.createInjector(config);
		
		//sentence predictor
		SentencePredictor sp = (SentencePredictor)injector.getInstance(SentencePredictor.class);
		
		String[] texts = {"Cells appear as straight rods (0.5-3.0 x 0.4-0.5 µm), which occur singly or in pairs.",
//				"Cells are arranged singly, in short chains or in groups.",
//				"One species can grow actively on media containing 1-7% NaCl (w/v).",
//				"GRowth occurs at 20-50 celsius_degree (optimum 40-45 celsius_degree ), at pH 5.9-8.8 (optimum pH 7.0) and with up to 5% (w/v) NaCl.",
//				"Grows in media prepared with 2× strength sea salts, but no growth occurs with 4× strength sea salts.",
//				"Requires sea salts for growth.",
//				"Media supplemented only with Na+ ions do not support growth. ",
//				"Requires Na+ ions for growth.",
//				"growth is inhibited in the absence of NaCl and in the presence of >8 % (w/v) NaCl.",
//				"The minimal Mg2+ concentration for growth and the Mg2+ concentration for optimal growth are 5 and 20 mM, respectively.",
//				"Requires Na+ and Mg2+ ions for growth.",
//				"Growth occurs in 1.7– 5.0 M NaCl (optimum, 2.6 M), in 0.01–1.0 M Mg2+ (optimum, 0.2–0.5 M), at pH 7.0–9.0 (optimum, pH 7.0– 8.0) and at 20–42 ˚C (optimum, 37 ˚C)."
				//"Grows in the presence of 0–2%(w/v) NaCl, with optimum growth in the absence of NaCl.",
				//"Halophilic, growing between 1.0 and 7.5 % (w/v) NaCl with optimum growth at 1–3 %.",
				//"growth does not occur in the absence of NaCl or in the presence of > 7%(w/v) NaCl."
				//"Acetoin is produced."
				"Gelatin hydrolysis, Voges–Proskauer test and citrate utilization results are positive, but activities of ONPG, arginine dihydrolase, lysine decarboxylase, ornithine decarboxylase and urease, production of hydrogen sulfide and indole and reduction of nitrate and nitrite are negative.",
				"The major end products are butyric and isobutyric acids; smaller amounts of acetic, propionic and succinic acids are produced."		
		};
		
		CategoryPredictor cp = (CategoryPredictor)injector.getInstance(CategoryPredictor.class);
		
		
		
		for(String text:texts){
			MultiClassifiedSentence sent = new MultiClassifiedSentence(text);
			Set<ILabel> characterLabels = sp.predict(sent);
			Set<ILabel> categoryLabels = cp.predict(sent);
			
			System.out.println(text+"|"+characterLabels+"|"+categoryLabels);
		}
	}

}
