package com.restfulrobot.cdcapplication;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.restfulrobot.cdcapplication.comparators.AddressComparator;
import com.restfulrobot.cdcapplication.comparators.DistanceComparator;
import com.restfulrobot.cdcapplication.comparators.IdComparator;
import com.restfulrobot.cdcapplication.objects.Coordinar;
import com.restfulrobot.cdcapplication.objects.PlaceItem;

import java.util.ArrayList;

public class PlaceItemsListFragment extends ListFragment {

    // индитификаторы
    final int MENU_DELETE = 1;
    final int MENU_CHANGE_COMMENT = 2;

    // индитификатор выбранного элемента
    private int selectedPlaceItemId = 99999;
    // View выбранного элемента
    private View selectedPlaceItemView = null;
    // дефолтный background для неактивного элемента
    private Drawable defaultBackground;
    // адаптер для ListView
    private PlaceItemsAdapter adapter;
    // список мест
    private ArrayList<PlaceItem> items;
    // координаты последней выбранной точки
    private Coordinar lastCoordinar;
    // ссылка на главную активность
    private MainActivity mainActivity;
    // диалоговое окно для изменения комментария
    private PlaceCommentDialog placeCommentDialog;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        items = new ArrayList<PlaceItem>();
        adapter = new PlaceItemsAdapter(
                getActivity(), R.layout.place_list_item,
                items);
        adapter.setNotifyOnChange(true);
        setListAdapter(adapter);
        registerForContextMenu(getListView());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mainActivity = (MainActivity) context;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        // инициализируем дефолтный background
        defaultBackground = getListView().getBackground();
        // регистрируем контекстное меню
        registerForContextMenu(getListView());
    }

    @Override
    public void onListItemClick(ListView l, View view, int position, long id) {
        // получаем точку для текущей позиции в списке
        PlaceItem currentPlaceItem = adapter.getItem(position);
        // выделяем цветом текущий элемент
        selectedPlaceItemId = currentPlaceItem.getId();
        if (selectedPlaceItemView != null) {
            selectedPlaceItemView.setBackground(defaultBackground);
        }
        selectedPlaceItemView = view;
        selectedPlaceItemView.setBackgroundResource(R.drawable.active_item);
        // запрос на изменение карты
        mainActivity.goToPlaceQuery(currentPlaceItem.getId(),
                currentPlaceItem.getComment(),
                currentPlaceItem.getCoordinar().getLatitude(),
                currentPlaceItem.getCoordinar().getLongitude());
        lastCoordinar = currentPlaceItem.getCoordinar();
        // обновляем список (метод введен из-за одного глюка на устройстве)
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, MENU_DELETE, 0, "Удалить");
        menu.add(0, MENU_CHANGE_COMMENT, 0, "Изменить комментарий");
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int pos = info.position;
        // получаем текщую точку
        PlaceItem selectedPlaceItem = items.get(pos);
        switch (item.getItemId()) {
            case MENU_DELETE:
                // если данная точка активна, то вызываем метод deletePlaceFromMapQuery
                if (selectedPlaceItem.getId() == selectedPlaceItemId) {
                    lastCoordinar = null;
                    selectedPlaceItemId = 99999;
                    selectedPlaceItemView = null;
                    mainActivity.deletePlaceFromMapQuery();
                }
                adapter.remove(selectedPlaceItem);
                break;
            case MENU_CHANGE_COMMENT:
                boolean activeItemFlag = lastCoordinar.equals(selectedPlaceItem.getCoordinar());
                placeCommentDialog = new PlaceCommentDialog();
                placeCommentDialog.setPlaceItemForChanging(selectedPlaceItem, activeItemFlag);
                placeCommentDialog.show(getFragmentManager(), "PlaceCommentDialog");
                break;
        }
        return super.onContextItemSelected(item);
    }

    // добавляем новую точку, при этом снимаем выделение с выбранной
    public void addNewItem(PlaceItem placeItem) {
        lastCoordinar = placeItem.getCoordinar();
        selectedPlaceItemId = 99999;
        selectedPlaceItemView = null;
        adapter.add(placeItem);
    }

    // сортировка элементов в ListView
    public void sort(TypeOfSort type) {
        switch (type) {
            case SortById:
                adapter.sort(new IdComparator());
                break;
            case SortByAddress:
                adapter.sort(new AddressComparator());
                break;
            case SortByDistanceFromMe:
                adapter.sort(new DistanceComparator());
                break;
        }
    }

    // проверка: имеет ли смысл сортировать точки
    public boolean isSortable() {
        if (items.size() > 1) {
            return true;
        }
        return false;
    }

    // проверка: есть ли координаты для перехода к навигатору
    public boolean isPointExist() {
        if (lastCoordinar == null) {
            return false;
        }
        return true;
    }

    // сформировать массив координат для всех точек
    public String getQueryArgumentsForSearchingDistance() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        String latitude;
        String longitude;
        int lastElementIndex = items.size() - 1;
        for (int i = 0; i < lastElementIndex; i++) {
            latitude = String.valueOf(items.get(i).getCoordinar().getLatitude());
            longitude = String.valueOf(items.get(i).getCoordinar().getLongitude());
            sb.append(latitude + "," + longitude + ",");
        }
        latitude = String.valueOf(items.get(lastElementIndex).getCoordinar().getLatitude());
        longitude = String.valueOf(items.get(lastElementIndex).getCoordinar().getLongitude());
        sb.append(latitude + "," + longitude + "]");
        return sb.toString();
    }

    // установить для всех точек координаты
    public void setDistances(double[] distances) {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setDistance(distances[i]);
        }
    }

    // достаём координаты для последней выбранной точки
    public Coordinar getLastCoordinar() {
        return lastCoordinar;
    }

    // закрыть диалоговое окно
    public void dismissDialog() {
        placeCommentDialog.dismiss();
    }

    // вложенный класс с адаптером для ListView
    private class PlaceItemsAdapter extends ArrayAdapter<PlaceItem> {

        private final int viewResourceId;

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public PlaceItem getItem(int position) {
            return items.get(position);
        }

        public PlaceItemsAdapter(Context context, int viewResourceId, ArrayList<PlaceItem> items) {
            super(context, viewResourceId, items);
            this.viewResourceId = viewResourceId;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(viewResourceId, null);
            }
            PlaceItem placeItem = getItem(position);
            TextView placeIdTextView = (TextView) view.findViewById(R.id.placeId);
            placeIdTextView.setText(String.valueOf(placeItem.getId()));
            TextView placeAddressView = (TextView) view.findViewById(R.id.placeAddress);
            placeAddressView.setText(placeItem.getAddress());
            if (selectedPlaceItemId == placeItem.getId()) {
                view.setBackgroundResource(R.drawable.active_item);
                selectedPlaceItemView = view;
            } else {
                view.setBackground(defaultBackground);
            }
            return view;
        }

    }
}
