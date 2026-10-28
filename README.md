# Vesper Cube

Kırmızı, iki blokluk bir küp. Oyuncuyu hedef alır, peşinden gelir ve önüne çıkan blokları yiyip yok eder.
Videodaki "Vesper" tarzı kırmızı küp yaratığın Fabric modu olarak yeniden yapımı.

**Minecraft 1.21.5 + Fabric** için derlenir.

![Vesper Cube ikonu](src/main/resources/assets/vespercube/icon.png)

## Özellikler

- **Kırmızı küp yaratık** (`vespercube:red_cube`): 80 can, 9 saldırı hasarı, yüksek geri tepme direnci.
- **Blok yeme**: hedef kovalarken önündeki bloğu kırar (bedrock gibi kırılamaz bloklar hariç), ses ve parçacık efektiyle.
- **Saldırı AI'ı**: hedef seçimi, yakın dövüş, geri dönme (revenge), gezinme, su içinde yüzme.
- **Çağırma**: yaratıcı envanterindeki `Spawn Eggs` sekmesindeki yumurta ya da `/summon` komutu.
- **TR/EN dil dosyaları**.

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

Sunucuda da aynı jar + Fabric API gerekir; istemci ve sunucu tarafı aynı sürümde olmalı.

## Derleme

Gradle 8.14 ve JDK 21 gerekir (depoda wrapper yok, isteğe bağlı olarak `gradle wrapper` ile üretebilirsin):

```bash
gradle build
```

Çıkan jar: `build/libs/vesper-cube-1.1.0.jar`

Geliştirme için:

```bash
gradle runClient   # modlu istemciyi başlatır
gradle runServer   # modlu sunucu
```

## Kullanım

- Yaratıcı modda `Vesper Küpü Çağırma Yumurtası` ile çağır, ya da:
- Komut: `/summon vespercube:red_cube`

Doğal doğma henüz eklenmedi; yaratık spawn egg veya komutla çağrılıyor.

Doğduktan sonra yakınındaki oyuncuyu hedefler; hedefi varken önündeki blokları yemeye başlar.
Blokların arkasına saklanmak onu durdurmaz: duvarı yer.

## 1.21.5 notları

1.21.5 render hattı render state tabanlıdır: model `RedCubeRenderState` üzerinden çizilir
(`EntityModel<RedCubeRenderState>`), renderer üç tip parametresi alır
(`MobEntityRenderer<RedCubeEntity, RedCubeRenderState, RedCubeModel>`).
Spawn egg renkleri artık kodda değil, `assets/vespercube/items/red_cube_spawn_egg.json`
model tanımı ve `textures/item/red_cube_spawn_egg.png` dokusuyla tanımlanır.

## Proje yapısı

```
src/main/java/com/vespercube/
├── VesperCubeMod.java              # kayıt: entity, spawn egg, nitelikler
├── entity/RedCubeEntity.java       # AI hedefleri + blok yeme
└── client/
    ├── VesperCubeClient.java       # model katmanı + renderer kaydı
    ├── RedCubeModel.java           # tek küplük model (32x32x32 birim)
    ├── RedCubeRenderState.java     # render state (1.21.5)
    └── RedCubeRenderer.java        # doku bağlama
src/main/resources/assets/vespercube/
├── textures/entity/red_cube.png    # 128x64 doku ağı (yüz ön yüzde)
├── textures/item/red_cube_spawn_egg.png
├── items/red_cube_spawn_egg.json   # item model tanımı
├── models/item/red_cube_spawn_egg.json
├── icon.png                        # mod ikonu
└── lang/{en_us,tr_tr}.json
tools/gen_assets.py                 # dokuları yeniden üretir (bağımlılıksız Python)
```

Dokuları değiştirdikten sonra: `python3 tools/gen_assets.py`

## GitHub üzerinden derleme

Her push'ta **Actions → build** iş akışı jar'ı üretir ve artifact olarak yükler.

Sürüm çıkarmak için etiket at (jar otomatik olarak GitHub Release'e eklenir):

```bash
git tag v1.1.0
git push origin v1.1.0
```

## Lisans

MIT. Bu proje Mojang veya Microsoft ile bağlantılı değildir.
