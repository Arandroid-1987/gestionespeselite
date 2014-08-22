package utils.file.export;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;

import utils.DateUtils;
import utils.NumberUtils;

import com.dto.Ricavo;
import com.dto.Spesa;
import com.dto.TagRicavo;
import com.dto.TagSpesa;

public class TXTExport {

	private Collection<Spesa> spese;
	private Collection<Ricavo> ricavi;
	private File file;
	private double sommaSpese = 0;
	private double sommaRicavi = 0;
	private String startDate;
	private String endDate;
	public final static String EXTENSION = ".txt";

	public TXTExport(Collection<Spesa> spese, Collection<Ricavo> ricavi,
			File file, String startDate, String endDate) {
		super();
		this.spese = spese;
		this.ricavi = ricavi;
		this.file = file;
		this.startDate = startDate;
		this.endDate = endDate;
		String oldName = file.getAbsolutePath();
		if (startDate != null && endDate != null) {
			this.file = new File(oldName.substring(0, oldName.length() - 4)
					+ "_" + this.startDate + "_" + this.endDate + EXTENSION);
		}
	}

	public boolean export() {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file);
			pw.println("SPESE");
			pw.println();
			int i = 1;
			for (Spesa spesa : spese) {
				String dataOriginale = spesa.getData();
				String dataNuova = DateUtils
						.getPrintableDataFormat(dataOriginale);

				String descrizione = spesa.getDescrizione();
				String tagString = null;
				if (descrizione == null || descrizione.length() == 0) {

					StringBuilder builder = new StringBuilder();

					Collection<TagSpesa> tags = spesa.getTags();
					int count = 0;
					for (TagSpesa tag : tags) {
						builder.append(tag.getValore());
						if (count < tags.size() - 1)
							builder.append(" - ");
						count++;
					}
					tagString = builder.toString();
				} else {
					tagString = descrizione;
				}
				String importo = NumberUtils.getString(spesa.getImporto())
						+ " euro";
				sommaSpese += spesa.getImporto();
				pw.println(i + ")\t\tDATA: " + dataNuova);
				pw.println("\t\tTAG: " + tagString);
				pw.println("\t\tIMPORTO: " + importo);
				i++;
			}
			pw.println();
			pw.println("TOTALE SPESE: " + NumberUtils.getString(sommaSpese)
					+ " euro");
			pw.println();
			pw.println("RICAVI");
			pw.println();
			i = 1;
			for (Ricavo ricavo : ricavi) {
				String dataOriginale = ricavo.getData();
				String dataNuova = DateUtils
						.getPrintableDataFormat(dataOriginale);

				String descrizione = ricavo.getDescrizione();
				String tagString = null;
				if (descrizione == null || descrizione.length() == 0) {
					StringBuilder builder = new StringBuilder();

					Collection<TagRicavo> tags = ricavo.getTags();
					int count = 0;
					for (TagRicavo tag : tags) {
						builder.append(tag.getValore());
						if (count < tags.size() - 1)
							builder.append(" - ");
						count++;
					}
					tagString = builder.toString();
				}
				else{
					tagString = descrizione;
				}
				String importo = NumberUtils.getString(ricavo.getImporto())
						+ " euro";
				sommaRicavi += ricavo.getImporto();
				pw.println(i + ")\t\tDATA: " + dataNuova);
				pw.println("\t\tTAG: " + tagString);
				pw.println("\t\tIMPORTO: " + importo);
				i++;
			}
			pw.println();
			pw.println("TOTALE RICAVI: " + NumberUtils.getString(sommaRicavi)
					+ " euro");
			pw.println();
			double bilancio = sommaRicavi - sommaSpese;
			pw.println("BILANCIO: " + NumberUtils.getString(bilancio) + " euro");
		} catch (Exception e) {
			return false;
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
		return true;
	}

}
