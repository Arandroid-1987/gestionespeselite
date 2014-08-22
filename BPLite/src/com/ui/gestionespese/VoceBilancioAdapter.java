package com.ui.gestionespese;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.londatiga.android.ActionItem;
import net.londatiga.android.PopupMenu;
import utils.CustomToast;
import utils.DateUtils;
import utils.NumberUtils;
import utils.Version;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arandroid.bilanciopersonale.AddRicavoActivity;
import com.arandroid.bilanciopersonale.AddSpesaActivity;
import com.arandroid.bplite.R;
import com.dao.RicavoDao;
import com.dao.RicavoProgrammatoDao;
import com.dao.SettingsDao;
import com.dao.SpesaDao;
import com.dao.SpesaProgrammataDao;
import com.db.DatabaseHandler;
import com.dto.Ricavo;
import com.dto.RicavoProgrammato;
import com.dto.Spesa;
import com.dto.SpesaProgrammata;
import com.dto.TagRicavo;
import com.dto.TagSpesa;
import com.dto.VoceBilancio;
import com.fortysevendeg.swipelistview.SwipeListView;

public class VoceBilancioAdapter extends ArrayAdapter<VoceBilancio> {

	private CustomToast customToast;
	private Toast toast;
	private SwipeListView parentList;
	private Context context;
	private boolean isSpesa;

	private boolean proVersion = false;
	public boolean tutorialOn = true;
	public boolean animate;

	private String[] currency;

	private List<VoceBilancio> items = new ArrayList<VoceBilancio>();

	static class ViewHolder {
		TextView dataTW;
		TextView importoTW;
		TextView firstTagTW;
		Button mostraTag;
		Button elimina;
		Button duplica;
		Button modifica;
	}

	public VoceBilancioAdapter(Context context, int textViewResourceId,
			List<VoceBilancio> objects, boolean isSpesa) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.items = objects;
		customToast = new CustomToast((Activity) context,
				context.getString(R.string.rimozione_avvenuta_con_successo),
				context.getString(R.string.errore_rimozione_dati));
		this.isSpesa = isSpesa;
		proVersion = context.getResources().getString(R.string.version)
				.equals(Version.PRO);
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getReadableDatabase();
		animate = SettingsDao.isTutorialOn(db);
		currency = SettingsDao.getCurrency(db);
		db.close();
	}

	public int getCount() {
		return this.items.size();
	}

	public VoceBilancio getItem(int index) {
		return this.items.get(index);
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			LayoutInflater li = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = li.inflate(R.layout.row_spesa_ricavo, parent, false);
			holder = new ViewHolder();
			holder.dataTW = (TextView) convertView.findViewById(R.id.data);
			holder.importoTW = (TextView) convertView
					.findViewById(R.id.importo);
			holder.mostraTag = (Button) convertView
					.findViewById(R.id.mostraTagButton);
			holder.firstTagTW = (TextView) convertView
					.findViewById(R.id.firstTag);
			holder.elimina = (Button) convertView.findViewById(R.id.elimina);
			holder.duplica = (Button) convertView.findViewById(R.id.duplica);
			holder.modifica = (Button) convertView.findViewById(R.id.modifica);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final VoceBilancio item = getItem(position);

		if (item instanceof SpesaProgrammata
				|| item instanceof RicavoProgrammato) {
			holder.duplica.setVisibility(View.GONE);
		}

		RelativeLayout layout = (RelativeLayout) convertView
				.findViewById(R.id.rowSpesa);
		if (animate && tutorialOn) {
			Animation animation = AnimationUtils.loadAnimation(context,
					R.anim.translate);
			layout.startAnimation(animation);
			animate = false;
		}
		String newData = DateUtils.getPrintableDataFormat(item.getData());
		holder.dataTW.setText(context.getString(R.string.data_uc_column_space)
				+ newData);
		String space_currency = " " + currency[1];
		holder.importoTW.setText(NumberUtils.getString(item.getImporto())
				+ space_currency);

		parentList = ((SwipeListView) parent);

		holder.elimina.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				parentList.closeAnimate(position);
				DatabaseHandler dbHandler = DatabaseHandler
						.getInstance(context);
				// apro il DB sia in lettura che in scrittura
				SQLiteDatabase db = dbHandler.getWritableDatabase();
				try {
					if (isSpesa) {
						if (item instanceof SpesaProgrammata) {
							if (SpesaProgrammataDao.deleteSpesa(db,
									item.getId())) {
							} else {
								toast = customToast.getErrorToast();
								toast.show();
							}
						} else {
							SpesaProgrammata s = SpesaProgrammataDao.getSpesa(
									db, item.getId());
							if (s != null) {
								createAndShowVoceProgrammataDialog(s);
							}
							if (!SpesaDao.deleteSpesa(db, item.getId())) {
								toast = customToast.getErrorToast();
								toast.show();
							}
						}
					} else {
						if (item instanceof RicavoProgrammato) {
							if (RicavoProgrammatoDao.deleteRicavo(db,
									item.getId())) {
							} else {
								toast = customToast.getErrorToast();
								toast.show();
							}
						} else {
							RicavoProgrammato r = RicavoProgrammatoDao
									.getRicavo(db, item.getId());
							if (r != null) {
								createAndShowVoceProgrammataDialog(r);
							}
							if (!RicavoDao.deleteRicavo(db, item.getId())) {
								toast = customToast.getErrorToast();
								toast.show();
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					db.close();
				}
			}

		});

		holder.modifica.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (proVersion) {
					parentList.closeAnimate(position);
					Intent intent;
					if(isSpesa){
						intent = new Intent(context, AddSpesaActivity.class);
					}
					else{
						intent = new Intent(context, AddRicavoActivity.class);
					}
					intent.putExtra("vocebilancio", item);
					intent.putExtra("copy", false);
					context.startActivity(intent);
				} else {
					showBuyProDialog();
				}
			}
		});

		holder.duplica.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (proVersion) {
					parentList.closeAnimate(position);
					VoceBilancio copy = null;
					Intent intent = null;
					if (item instanceof Ricavo) {
						copy = new Ricavo();
						Ricavo r = (Ricavo) copy;
						r.setId(System.currentTimeMillis());
						r.setData(item.getData());
						r.setImporto(item.getImporto());
						Ricavo current = (Ricavo) item;
						r.setTags(current.getTags());
						intent = new Intent(context, AddRicavoActivity.class);
					} else if (item instanceof Spesa) {
						copy = new Spesa();
						Spesa s = (Spesa) copy;
						copy.setId(System.currentTimeMillis());
						copy.setData(item.getData());
						copy.setImporto(item.getImporto());
						Spesa current = (Spesa) item;
						s.setTags(current.getTags());
						intent = new Intent(context, AddSpesaActivity.class);
					}
					intent.putExtra("vocebilancio", copy);
					intent.putExtra("copy", true);
					context.startActivity(intent);
				} else {
					showBuyProDialog();
				}
			}
		});

		final Collection<String> ris = new LinkedList<String>();
		if (item instanceof Spesa) {
			Spesa s = (Spesa) item;
			Collection<TagSpesa> tags = s.getTags();
			for (TagSpesa tag : tags) {
				ris.add(tag.getValore());
			}
		} else if (item instanceof Ricavo) {
			Ricavo r = (Ricavo) item;
			Collection<TagRicavo> tags = r.getTags();
			for (TagRicavo tag : tags) {
				ris.add(tag.getValore());
			}
		}
		String etc = ris.size() == 1 ? "" : "...";
		Iterator<String> it = ris.iterator();
		String firstTag = "";
		if (it.hasNext()) {
			firstTag = it.next();
		}
		String descrizione = item.getDescrizione();
		if (descrizione != null && descrizione.length() > 0) {
			holder.firstTagTW.setText(descrizione);
		} else {
			holder.firstTagTW.setText(context
					.getString(R.string.tag_uc_column_space) + firstTag + etc);
		}
		holder.mostraTag.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PopupMenu menu = createPopupMenu(ris, holder.mostraTag);
				menu.show();
			}
		});

		return convertView;
	}

	protected void createAndShowVoceProgrammataDialog(final VoceBilancio v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context
				.getString(R.string.vuoi_eliminare_voce_programmata));
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				DatabaseHandler handler = DatabaseHandler.getInstance(context);
				SQLiteDatabase db = handler.getWritableDatabase();
				if (v instanceof SpesaProgrammata) {
					SpesaProgrammata s = (SpesaProgrammata) v;
					SpesaProgrammataDao.deleteSpesa(db, s.getId());
				} else if (v instanceof RicavoProgrammato) {
					RicavoProgrammato r = (RicavoProgrammato) v;
					RicavoProgrammatoDao.deleteRicavo(db, r.getId());
				}
				db.close();
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	private void showBuyProDialog() {
		BuyProDialogGenerator.create(context).show();
	}

	private PopupMenu createPopupMenu(Collection<String> tags, View v) {
		PopupMenu menu = new PopupMenu(v);
		for (String tag : tags) {
			ActionItem item = new ActionItem();
			item.setTitle(tag);
			item.setIcon(context.getResources().getDrawable(R.drawable.tag));
			menu.addActionItem(item);
		}
		return menu;
	}

}
