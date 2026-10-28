# Vesper Cube

Videodaki **Vesper** tarzı kırmızı küp: sadece bir mob değil, yanında gezen, **konuşan**, senin
için **ev inşa eden** bir refakatçi. Vurulduğunda çıldırıp etrafı yıkmaya başlar; ayrıca onunla
birlikte bir **arena boyutuna** ışınlanıp **Küp Tiranı** ile dövüşebilirsin.

**Minecraft 1.21.5 + Fabric** için derlenir.

![Vesper Cube ikonu](src/main/resources/assets/vespercube/icon.png)

## Ne yapıyor?

- **Takip**: en yakın oyuncuyu bulur ve 4 blok mesafeyi koruyarak peşinden gelir.
- **Konuşma + ağız animasyonu**: konuşurken kafasının üzerinde yazı çıkar ve **ağzı açılıp
  kapanır**. Ağız, iki ayrı doku (`red_cube.png` / `red_cube_talking.png`) arasında geçiş
  yapılarak canlandırılır; konuşma durumu `TrackedData` ile istemcilere senkronlanır, yani
  herkes aynı anda ağzının açıldığını görür.
- **Ev inşası**: ~1 dakikada bir, yanına 5×5'lik bir kulübe kurar — taş zemini, tahta duvarları,
  cam pencereleri, 2 blokluk kapı boşluğu ve tahta çatısıyla. Blokları tek tek, ses ve parçacık
  efektiyle yerleştirir (blok başına 2 tick, toplam ~100 blok).
- **İnşa modu**: sağ tık ile inşayı açıp kapatabilirsin; Vesper bunu söyler.
- **Kızma**: canı düştüğünde 12 saniye boyunca etrafındaki blokları yemeye başlar (bedrock gibi
  kırılamaz bloklar hariç); sonra sakinleşir.
- **Arena + boss**: `/vesper arena` seni ve Vesper'ı `vespercube:arena` boyutuna ışınlar, zemini
  hazırlar ve **Küp Tiranı**'nı uyandırır. `/vesper leave` geri getirir.
- **TR/EN** dil dosyaları.

## Komutlar

| Komut | Ne yapar |
| --- | --- |
| `/vesper say <metin>` | En yakın Vesper'ı konuşturur (ağzı oynar, yazı çıkar) |
| `/vesper arena` | Seni + Vesper'ı arena boyutuna ışınlar, boss'u çağırır |
| `/vesper leave` | Arenadan ana dünyanın başlangıç noktasına döner |

## Küp Tiranı (boss)

- 320 can, 12 hasar, 8 zırh, geri tepme bağışıklığı; kafasının üstünde **boss bar**.
- Yaklaşık her 6 saniyede bir **yer sarsma**: 5 blok yarıçapta hasar + oyuncuları havaya fırlatma.
- Canı yarıya indiğinde **öfkelenir**: ses, parçacık, daha hızlı saldırı (3,5 saniye) ve daha geniş
  sarsma yarıçapı.
- Öldüğünde 1 netherite külçesi + 16 zümrüt düşürür, yakındaki oyunculara mesaj gider.

## Gereksinimler

| Gereksinim | Sürüm |
| --- | --- |
| Minecraft | 1.21.5 |
| Fabric Loader | 0.20.0+ |
| Fabric API | zorunlu (0.128.x) |
| Java | 21 |

## Kurulum

1. [Fabric Loader](https://fabricmc.net/use/installer/) ile Minecraft **1.21.5** profili oluştur.
2. `mods` klasörüne **Fabric API** ve derlenen `vesper-cube-1.2.0.jar` dosyasını at.
3. Oyunu Fabric profiliyle başlat.

Sunucu ve istemci aynı sürümde olmalı; sunucuda da aynı jar + Fabric API gerekir.

## Kullanım

- Yaratıcı modda `Vesper Çağırma Yumurtası` ile çağır, ya da `/summon vespercube:red_cube`
- Sağ tık: inşa modunu açar/kapatır
- Konuşsun diye: `/vesper say merhaba`
- Boss dövüşü: `/vesper arena`, sonra `/vesper leave`

## Derleme

Gradle 8.14 ve JDK 21 gerekir:

```bash
gradle build
```

Çıkan jar: `build/libs/vesper-cube-1.2.0.jar`

```bash
gradle runClient   # modlu istemci
gradle runServer   # modlu sunucu
```

## Teknik notlar

- 1.21.5 render hattı render state tabanlı: model `RedCubeRenderState` üzerinden çizilir.
  `RedCubeModel<T extends RedCubeRenderState>` generic olduğu için hem refakatçi hem boss aynı
  modeli, farklı render state ve dokularla kullanır.
- Ağız animasyonu: `RedCubeEntity` içindeki `TALKING` (`TrackedData<Boolean>`) senkronlanır,
  renderer `state.mouthOpen` alanını doldurur ve `getTexture` açık ağızlı dokuyu döndürür.
- Spawn egg renkleri kodda değil; `assets/vespercube/items/*.json` model tanımları ve
  `textures/item/*.png` dokularıyla geliyor (1.21.5 davranışı).
- Arena boyutu tamamen veri paketiyle tanımlı: `data/vespercube/dimension/arena.json` (boş
  `minecraft:flat` dünyası) + `data/vespercube/dimension_type/arena.json` (karanlık, gökyüzü
  olmayan, `the_end` efektli). Komut zemini ve dört sütunu kodla kurar, böylece dünya üretimine
  bağımlı değildir.
- Yapay zekâ servisi kullanılmıyor: davranışlar oyun içi kurallarla yazıldı.

## Proje yapısı

```
src/main/java/com/vespercube/
├── VesperCubeMod.java                    # kayıt: iki entity, iki spawn egg, komutlar
├── VesperCommands.java                   # /vesper say | arena | leave
├── entity/RedCubeEntity.java             # takip + inşa + konuşma + kızma
├── entity/CubeTyrantEntity.java          # boss: boss bar, sarsma, öfke fazı
├── entity/HouseBlueprint.java            # 5x5 kulübenin blok listesi
└── client/
    ├── VesperCubeClient.java             # model katmanı + renderer kayıtları
    ├── RedCubeModel.java                 # tek küplük, generic model
    ├── RedCubeRenderState.java           # render state + mouthOpen
    ├── RedCubeRenderer.java              # Vesper dokuları
    ├── CubeTyrantRenderState.java
    └── CubeTyrantRenderer.java           # boss dokuları + 1.6x ölçek
src/main/resources/
├── assets/vespercube/textures/entity/    # red_cube(_talking).png, cube_tyrant(_talking).png
├── assets/vespercube/{items,models,lang}
└── data/vespercube/{dimension,dimension_type}/arena.json
tools/gen_assets.py                       # dokuları yeniden üretir (bağımlılıksız Python)
```

Dokuları değiştirdikten sonra: `python3 tools/gen_assets.py`

## Sınırlar

- **Sesli sohbet yok.** Videodaki "mikrofonla Türkçe konuş, İngilizce cevap versin" davranışı bu
  sürümde yok: mikrofon kaydı + konuşma tanıma + metin-konuşma için harici bir servis ve API
  anahtarı gerekiyor. Şu an konuşma, `/vesper say` komutuyla ve hazır repliklerle çalışıyor;
  ağız animasyonu altyapısı hazır, bir metin-konuşma kaynağı bağlanınca aynı animasyon kullanılır.
- Oyun içi test bu ortamda yapılamadı (Minecraft istemcisi yok); doğrulama derleme + jar içeriği
  ile sınırlı.

## GitHub üzerinden derleme

Her push'ta **Actions → build** iş akışı jar'ı üretir. Sürüm için:

```bash
git tag v1.2.0
git push origin v1.2.0
```

## Lisans

MIT. Bu proje Mojang veya Microsoft ile bağlantılı değildir.
