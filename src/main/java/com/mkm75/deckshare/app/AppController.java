package com.mkm75.deckshare.app;

import com.mkm75.deckshare.core.DeckIO;
import com.mkm75.deckshare.core.DeckSerializer;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * JavaFX GUI におけるコントローラークラスです。<br><br>
 * 技術上AppControllerをFXAppのインナークラスにすることは可能ですが、fxmlにおけるcontroller指定はバイナリ名のため
 * AppController$FXAppのように指定する必要があります。ただし、一部のエディタはcontroller指定を完全限定名として扱っているため、
 * fxmlにおけるバグが発見しづらくなります。要は物好きでも無い限りインナークラスにするなってことです。
 */
public class AppController implements Initializable {

    @FXML
    public ListView<String> list;
    @FXML
    public TextArea text;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        var list = Arrays.stream(DeckIO.Slot.values())
                .map(DeckIO.Slot::toString)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        this.list.setItems(list);
        this.list.getSelectionModel().selectFirst();
        text.setWrapText(true);
    }

    private DeckIO.Slot getSlot() {
        String selected = list.getSelectionModel().getSelectedItem();
        if (selected == null) return null;
        try {
            return DeckIO.Slot.valueOf(selected.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
    @FXML
    protected void save(ActionEvent ignored) {
        try {
            DeckIO.Slot slot = getSlot();
            if (slot == null) return;
            var deck = DeckIO.get(slot);
            byte[] data = DeckSerializer.serialize(deck);
            String encoded = Base64.getEncoder().encodeToString(data);
            text.setText(encoded);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("ファイル取得に失敗しました");
            alert.show();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText("デッキコード化に成功しました");
        alert.show();
    }

    @FXML
    public void load(ActionEvent ignored) {
        try {
            DeckIO.Slot slot = getSlot();
            if (slot == null) return;
            String encoded = text.getText();
            byte[] data = Base64.getDecoder().decode(encoded);
            var deck = DeckSerializer.deserialize(data);
            DeckIO.set(slot, deck);
        } catch (ClassCastException | IllegalArgumentException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("コードが不正です");
            alert.show();
            return;
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("入出力中にエラーが発生しました");
            alert.show();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText("デッキコード読み込みに成功しました");
        alert.show();
    }

}
