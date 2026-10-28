# Vesper Cube

Videodaki **Vesper** tarzı kırmızı küp: sadece bir mob değil, yanında gezen ve senin için
**ev inşa eden** bir refakatçi. Vurulduğunda çıldırıp etrafı yıkmaya başlar.

**Minecraft 1.21.5 + Fabric** için derlenir.

![Vesper Cube ikonu](src/main/resources/assets/vespercube/icon.png)

## Ne yapıyor?

- **Takip**: en yakın oyuncuyu bulur ve 4 blok mesafeyi koruyarak peşinden gelir.
- **Ev inşası**: ~1 dakikada bir, yanına 5×5'lik bir kulübe kurar — taş zemini, tahta duvarları,
  cam pencereleri, 2 blokluk kapı boşluğu ve tahta çatısıyla. Blokları tek tek, ses ve parçacık
  efektiyle yerleştirir (blok başına 2 tick, toplam ~100 blok).
- **İnşa modu**: yumurtayla çağırdıktan sonra sağ tıklayarak inşayı açıp kapatabilirsin; sohbete
  "inşa modu açık/kapalı" yazar.
- **Kızma**: canı düştüğünde 12 saniye boyunca etrafındaki blokları yemeye başlar (bedrock gibi
  kırılamaz bloklar hariç); sonra sakinleşir.
- **Sohbet**: "Sana bir ev yapıyorum!", "Evin hazır." gibi satırları sohbete yazar.
- **TR/EN** dil dosyaları.

## Gereksinimler

| Gereksinim | Sürüm |
| --- | --- |
| Minecraft | 1.21.5 |
| Fabric Loader | 0.20.0+ |
| Fabric API | zorunlu (0.128.x) |
| Java | 21 |

## Kurulum

1. [Fabric Loader](https://fabricmc.net/use/installer/) ile Minecraft **1.21.5** profili oluştur.
2. `mods` klasörüne **Fabric API** ve derlenen `vesper-cube-1.1.0.jar` dosyasını at.
3. Oyunu Fabric profiliyle başlat.

Sunucu ve istemci aynı sürümde olmalı; sunucuda da aynı jar + Fabric API gerekir.

## Kullanım

- Yaratıcı modda `Vesper Çağırma Yumurtası` ile çağır, ya da `/summon vespercube:red_cube`
- Sağ tık: inşa modunu açar/kapatır
- Vesper kendi başına ev yapar; vurursan etrafı kırmaya başlar

## Derleme

Gradle 8.14 ve JDK 21 gerekir:

```bash
gradle build
```

Çıkan jar: `build/libs/vesper-cube-1.1.0.jar`

```bash
gradle runClient   # modlu istemci
gradle runServer   # modlu sunucu
```

## Teknik notlar

- 1.21.5 render hattı render state tabanlı: model `RedCubeRenderState` üzerinden çizilir
  (`EntityModel<RedCubeRenderState>`), renderer `MobEntityRenderer<VesperEntity, RedCubeRenderState, RedCubeModel>`.
- Spawn egg rengi kodda değil; `assets/vespercube/items/red_cube_spawn_egg.json` model tanımı ve
  `textures/item/red_cube_spawn_egg.png` dokusuyla geliyor.
- Ev planı `HouseBlueprint` içinde: zemini, duvarları, pencereleri ve çatısıyla tek bir blok listesi.
- Yapay zekâ servisi kullanılmıyor: davranışlar oyun içi kurallarla (takip, inşa, kızma) yazıldı.

## Proje yapısı

```
src/main/java/com/vespercube/
├── VesperCubeMod.java              # kayıt: entity, spawn egg, nitelikler
├── entity/RedCubeEntity.java       # takip + inşa + kızma davranışı
├── entity/HouseBlueprint.java      # 5x5 kulübenin blok listesi
└── client/
    ├── VesperCubeClient.java       # model katmanı + renderer kaydı
    ├── RedCubeModel.java           # tek küplük model (32x32x32 birim)
    ├── RedCubeRenderState.java     # render state (1.21.5)
    └── RedCubeRenderer.java        # doku bağlama
src/main/resources/assets/vespercube/
├── textures/entity/red_cube.png    # 128x64 doku ağı (yüz ön yüzde)
├── textures/item/red_cube_spawn_egg.png
├── items/red_cube_spawn_egg.json
├── models/item/red_cube_spawn_egg.json
├── icon.png
└── lang/{en_us,tr_tr}.json
tools/gen_assets.py                 # dokuları yeniden üretir (bağımlılıksız Python)
```

Dokuları değiştirdikten sonra: `python3 tools/gen_assets.py`

## GitHub üzerinden derleme

Her push'ta **Actions → build** iş akışı jar'ı üretir. Sürüm için:

```bash
git tag v1.1.0
git push origin v1.1.0
```

## Lisans

MIT. Bu proje Mojang veya Microsoft ile bağlantılı değildir.
