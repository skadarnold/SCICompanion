/***************************************************************************
	Copyright (c) 2020 Philip Fortier

	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
***************************************************************************/
#include "stdafx.h"
#include "ExtractAll.h"
#include "AppState.h"
#include "ResourceEntity.h"
#include "Components.h"
#include "PicOperations.h"
#include "View.h"
#include "RasterOperations.h"
#include "PaletteOperations.h"
#include "CompiledScript.h"
#include "Disassembler.h"
#include "Vocab000.h"
#include "ResourceContainer.h"
#include "Message.h"
#include "Text.h"
#include "ResourceBlob.h"
#include "Audio.h"
#include "SoundUtil.h"
#include "format.h"
#include "AudioCacheResourceSource.h"
#include <Src/Util/ImageUtil.h>
#include <atlimage.h>
void ExportViewResourceAsCelImages(const ResourceEntity& resource, PaletteComponent* optionalPalette, CString destinationFolder, bool extractAsPng)
{
    CelIndex celIndex = CelIndex(-1, -1);
        if (&resource)
        {
            const RasterComponent& raster = resource.GetComponent<RasterComponent>();
            int startLoop = (celIndex.loop == 0xffff) ? 0 : celIndex.loop;
            int endLoop = (celIndex.loop == 0xffff) ? raster.LoopCount() : (celIndex.loop + 1);
            for (int l = endLoop - 1; l >= startLoop; l--)
            {
                const Loop& loop = raster.Loops[l];
                celIndex.loop = l;
                celIndex.cel = -1;
                int startCelBase = -1;
                int endCelBase = -1;
                if (celIndex.loop != 0xffff)
                {
                    startCelBase = (celIndex.cel == 0xffff) ? 0 : celIndex.cel;
                    endCelBase = (celIndex.cel == 0xffff) ? (int)raster.Loops[celIndex.loop].Cels.size() : (celIndex.cel + 1);
                }
                int startCel = (startCelBase == -1) ? 0 : startCelBase;
                int endCel = (endCelBase == -1) ? (int)loop.Cels.size() : endCelBase;
                for (int c = endCel - 1; c >= startCel; c--)
                {
                    celIndex.loop = l;
                    celIndex.cel = c;
                    const Cel& cel = loop.Cels[c];
                    PaletteComponent* palette = optionalPalette;
                    if (!palette)
                    {
                        palette = &g_egaDummyPalette;
                    }
                    CBitmap bitmap;
                    SCIBitmapInfo bmi;
                    BYTE* pBitsDest;
                    bitmap.Attach(CreateBitmapFromResource(resource, celIndex, palette, &bmi, &pBitsDest));
                    if ((HBITMAP)bitmap)
                    {
                        Cel celEntire(size16((uint16_t)bmi.bmiHeader.biWidth, (uint16_t)bmi.bmiHeader.biHeight), point16(), 0);
                        celEntire.TransparentColor = cel.TransparentColor;
                        celEntire.Data.allocate(celEntire.GetDataSize());
                        celEntire.Data.assign(pBitsDest, pBitsDest + celEntire.GetDataSize());

						if (extractAsPng)
						{
							CString strFileName = destinationFolder + '/' + "view." + std::to_string(resource.ResourceNumber).c_str() + '.' + std::to_string(celIndex.loop).c_str() + '.' + std::to_string(celIndex.cel).c_str() + ".png";
							Save8BitBmpGdiP(strFileName, celEntire, *palette, false);
						}
						else
						{
							CString strFileNameBmp = destinationFolder + '/' + "view." + std::to_string(resource.ResourceNumber).c_str() + '.' + std::to_string(celIndex.loop).c_str() + '.' + std::to_string(celIndex.cel).c_str() + ".bmp";
							Save8BitBmp((std::string)strFileNameBmp, bmi, pBitsDest, 0);
						}
					}
				{
                    
				}
                }
            }
        }
}
void ExportFontResourceAsCelImages(const ResourceEntity& resource, PaletteComponent* optionalPalette, CString destinationFolder)
{
    CelIndex celIndex = CelIndex(-1, -1);
    if (&resource)
    {
        const RasterComponent& raster = resource.GetComponent<RasterComponent>();
        int startLoop = (celIndex.loop == 0xffff) ? 0 : celIndex.loop;
        int endLoop = (celIndex.loop == 0xffff) ? raster.LoopCount() : (celIndex.loop + 1);
        for (int l = endLoop - 1; l >= startLoop; l--)
        {
            const Loop& loop = raster.Loops[l];
            celIndex.loop = l;
            celIndex.cel = -1;
            int startCelBase = -1;
            int endCelBase = -1;
            if (celIndex.loop != 0xffff)
            {
                startCelBase = (celIndex.cel == 0xffff) ? 0 : celIndex.cel;
                endCelBase = (celIndex.cel == 0xffff) ? (int)raster.Loops[celIndex.loop].Cels.size() : (celIndex.cel + 1);
            }
            int startCel = (startCelBase == -1) ? 0 : startCelBase;
            int endCel = (endCelBase == -1) ? (int)loop.Cels.size() : endCelBase;
            for (int c = endCel - 1; c >= startCel; c--)
            {
                celIndex.loop = l;
                celIndex.cel = c;
                const Cel& cel = loop.Cels[c];
                PaletteComponent* palette = optionalPalette;
                if (!palette)
                {
                    palette = &g_egaDummyPalette;
                }
                CBitmap bitmap;
                SCIBitmapInfo bmi;
                BYTE* pBitsDest;
                bitmap.Attach(CreateBitmapFromResource(resource, celIndex, palette, &bmi, &pBitsDest));
                if ((HBITMAP)bitmap)
                {
                    Cel celEntire(size16((uint16_t)bmi.bmiHeader.biWidth, (uint16_t)bmi.bmiHeader.biHeight), point16(), 0);
                    celEntire.TransparentColor = cel.TransparentColor;
                    celEntire.Data.allocate(celEntire.GetDataSize());
                    celEntire.Data.assign(pBitsDest, pBitsDest + celEntire.GetDataSize());
                    std::string strFileName = destinationFolder + '/' + "font." + std::to_string(resource.ResourceNumber).c_str() + '.' + std::to_string(celIndex.cel).c_str() + ".png";
                    Save8BitBmpGdiP(strFileName.c_str(), celEntire, *palette, false);
                }
            }
        }
    }
}

HBITMAP convert_8to32(HBITMAP hbmp)
{
    BITMAP  bmp;
    if (GetObject(hbmp, sizeof(bmp), &bmp))
    {
        if (8 == bmp.bmBitsPixel)
        {
            int             cy = 0 < bmp.bmHeight ? bmp.bmHeight : -bmp.bmHeight;
            unsigned int    bpl8 = (bmp.bmWidth + 3) & ~3;
            unsigned int    bpl32 = 4 * bmp.bmWidth;
            unsigned char* lp8 = (unsigned char*)malloc(bpl8 * cy);
            unsigned char* lp32 = (unsigned char*)malloc(bpl32 * cy);
            HDC             mdc = CreateCompatibleDC(0);
            BITMAPINFO* pbmi = (BITMAPINFO*)malloc(sizeof(BITMAPINFO) + (256 * sizeof(RGBQUAD)));
            int             x, y;
            unsigned char* lpdst = lp32;
            unsigned char* lpsrc = lp8;
            pbmi->bmiHeader.biSize = sizeof(BITMAPINFO) + (256 * sizeof(RGBQUAD));
            GetDIBits(mdc, hbmp, 0, cy, lp8, pbmi, DIB_RGB_COLORS);
            for (y = 0; y < cy; y++)
            {
                for (x = 0; x < bmp.bmWidth; x++)
                {
                    lpdst[(x << 2) + 0] = pbmi->bmiColors[lpsrc[x]].rgbBlue;
                    lpdst[(x << 2) + 1] = pbmi->bmiColors[lpsrc[x]].rgbGreen;
                    lpdst[(x << 2) + 2] = pbmi->bmiColors[lpsrc[x]].rgbRed;
                    lpdst[(x << 2) + 3] = 0x00;
                }
                lpdst += bpl32;
                lpsrc += bpl8;
            }
            pbmi->bmiHeader.biSize = sizeof(BITMAPINFO);
            pbmi->bmiHeader.biPlanes = 1;
            pbmi->bmiHeader.biBitCount = 32;
            pbmi->bmiHeader.biCompression = 0;
            pbmi->bmiHeader.biSizeImage = bpl32 * cy;
            pbmi->bmiHeader.biClrUsed = 0;
            pbmi->bmiHeader.biClrImportant = 0;
            HBITMAP hbmp32 = CreateDIBitmap(mdc, &pbmi->bmiHeader, CBM_INIT, lp32, pbmi, DIB_RGB_COLORS);
            DeleteDC(mdc);
            free(pbmi);
            free(lp8);
            free(lp32);
            return hbmp32;
        }
    }
    return 0;
}

void ExportPNG32(CImage &image, const char *lpszFileName)
{
    if (image.GetBPP() < 32)
    {
        // Create a temporary CImage for manipulation
        CImage imgout;
        imgout.Create(image.GetWidth(), image.GetHeight(), 32, CImage::createAlphaChannel);

        // Copy the source image to the destination image
        HDC hdc = imgout.GetDC();
        image.BitBlt(hdc, 0, 0);
        imgout.ReleaseDC();

        // Manipulate the alpha channel based on pixel color
        BYTE *pImgBits = (BYTE *)imgout.GetBits();
        int pitch = imgout.GetPitch();
        for (int y = 0; y < imgout.GetHeight(); ++y)
        {
            for (int x = 0; x < imgout.GetWidth(); ++x)
            {
                // Calculate the pixel position in memory
                BYTE *pPixel = pImgBits + y * pitch + x * 4;

                // Get the pixel color
                COLORREF c1 = RGB(pPixel[2], pPixel[1], pPixel[0]); // BGR order

                // Manipulate the alpha channel based on pixel color
                pPixel[3] = (c1 == RGB(255, 255, 255)) ? 0 : 255;
            }
        }

        // Save the CImage object as PNG
	    imgout.Save(_T(lpszFileName), Gdiplus::ImageFormatPNG);
    
        image.Detach();
    }
}

unsigned long
hash(const char* str) {
    unsigned long hash = 5381;
    int c;
    while (c = *str++)
        hash = ((hash << 5) + hash) + c; /* hash * 33 + c */
    return hash;
}

void ExtractAllResources(SCIVersion version, const std::string &destinationFolderIn, bool extractResources, bool extractPicImages, bool extractViewImages, bool disassembleScripts, bool extractMessages, bool generateWavs, bool extractAsPng, IExtractProgress *progress)
{
	std::string destinationFolder = destinationFolderIn;
	if (destinationFolder.back() != '\\')
	{
		destinationFolder += "\\";
	}

	ObjectFileScriptLookups objectFileLookups(appState->GetResourceMap().Helper(), appState->GetResourceMap().GetCompiledScriptLookups()->GetSelectorTable());
	GlobalCompiledScriptLookups scriptLookups;
	if (disassembleScripts)
	{
		if (!scriptLookups.Load(appState->GetResourceMap().Helper()))
		{
			disassembleScripts = false;
		}
	}

	int totalCount = 0;
    auto resourceContainer = appState->GetResourceMap().Resources(ResourceTypeFlags::All, ResourceEnumFlags::IncludeCacheFiles);
	for (auto &blob : *resourceContainer)
	{
        if ((blob->GetType() == ResourceType::Text))
        {
            totalCount++;
        }
		if (extractResources)
		{
			totalCount++;
		}
		if (extractViewImages && (blob->GetType() == ResourceType::View))
		{
			totalCount++;
		}
		if (extractPicImages && (blob->GetType() == ResourceType::Pic))
		{
			totalCount++;
		}
		if (disassembleScripts && (blob->GetType() == ResourceType::Pic))
		{
			totalCount++;
		}
		if (extractMessages && (blob->GetType() == ResourceType::Message))
		{
			totalCount++;
		}
		if (generateWavs && (blob->GetType() == ResourceType::Audio))
		{
			totalCount++;
		}
	}

	// sync36/audio36
	if (generateWavs || extractResources)
	{
		resourceContainer = appState->GetResourceMap().Resources(ResourceTypeFlags::AudioMap, ResourceEnumFlags::MostRecentOnly | ResourceEnumFlags::ExcludePatchFiles);
		for (auto &blob : *resourceContainer)
		{
			if (blob->GetNumber() != version.AudioMapResourceNumber)
			{
				totalCount++;
				if (generateWavs)
				{
					totalCount++;
				}
			}
		}
	}

	int count = 0;
	// Get it again, because we don't supprot reset.
	resourceContainer = appState->GetResourceMap().Resources(ResourceTypeFlags::All, ResourceEnumFlags::MostRecentOnly | ResourceEnumFlags::ExcludePatchFiles);
	bool keepGoing = true;
	for (auto &blob : *resourceContainer)
	{
		std::string filename = GetFileNameFor(*blob);
		std::string fullPath = destinationFolder + filename;
		keepGoing = true;
		try
		{
			if (progress)
			{
				keepGoing = progress->SetProgress(fullPath, count, totalCount);
			}
			// Just the resource
			if (extractResources)
			{
				count++;
				blob->SaveToFile(fullPath);
			}

			if (keepGoing)
			{
                if (extractMessages && (blob->GetType() == ResourceType::Text))
                {
                    std::string possibleTextPath = fullPath + ".txt";
                    count++;
                    if (progress)
                    {
                        keepGoing = progress->SetProgress(possibleTextPath, count, totalCount);
                    }
                    std::unique_ptr<ResourceEntity> text = CreateResourceFromResourceData(*blob);
                    TextComponent& texttxt = text->GetComponent<TextComponent>();
                    for (size_t i = 0; i < texttxt.Texts.size(); i++)
                    {
                        std::string outStr = "";
                        const std::string str = texttxt.Texts[i].Text;
                        using std::cout; using std::ofstream;
                        using std::endl; using std::string;
                        char trimmedTextStr[250] = { '\0' };
                        sprintf_s(trimmedTextStr, "%d", hash(str.c_str()));
                        ofstream file_out;
                        outStr = str.c_str();
                        outStr += " = FILE : text.";
                        outStr += trimmedTextStr;
                        outStr += ".mp3\n\n";
                        file_out.open(possibleTextPath, std::ios_base::app);
                        file_out << outStr << endl;
                    }
                }

				// Then possible pictures
				CBitmap bitmap;
				SCIBitmapInfo bmi;
				BYTE *pBitsDest = nullptr;
                std::string possibleExt = ".bmp";

                if (extractAsPng)
                    possibleExt = ".png";
                    
				std::string possibleImagePath = fullPath + possibleExt;				

				if (extractPicImages && (blob->GetType() == ResourceType::Pic))
				{
					count++;
					if (progress)
					{
						keepGoing = progress->SetProgress(fullPath + "...", count, totalCount);
					}

					std::unique_ptr<ResourceEntity> resource = CreateResourceFromResourceData(*blob);
					PicComponent &pic = resource->GetComponent<PicComponent>();
					PaletteComponent *palette = resource->TryGetComponent<PaletteComponent>();
					bitmap.Attach(GetPicBitmap(PicScreen::Visual, pic, palette, pic.Size.cx, pic.Size.cy, &bmi, &pBitsDest));

                    if ((HBITMAP)bitmap)
				    {
                        //Save8BitBmp(possibleImagePath, bmi, pBitsDest, 0);
                        possibleImagePath = fullPath + possibleExt;
                        
                        CImage img;
                        img.Create(pic.Size.cx, pic.Size.cy, 32, CImage::createAlphaChannel);
                        img.Attach(bitmap);
                        ExportPNG32(img, _T(possibleImagePath.c_str()));

                        CBitmap bitmap_prio;
                        SCIBitmapInfo bmi_prio;
                        BYTE *pBitsDest_prio = nullptr;

                        std::unique_ptr<ResourceEntity> resource_prio = CreateResourceFromResourceData(*blob);
                        PicComponent &pic_prio = resource_prio->GetComponent<PicComponent>();
                        PaletteComponent *palette_prio = resource_prio->TryGetComponent<PaletteComponent>();
                        bitmap_prio.Attach(GetPicBitmap(PicScreen::Priority, pic_prio, palette_prio, pic_prio.Size.cx, pic_prio.Size.cy, &bmi_prio, &pBitsDest_prio));

                        CImage img_prio;
                        img_prio.Create(pic.Size.cx, pic.Size.cy, 32, CImage::createAlphaChannel);
                        img_prio.Attach(bitmap_prio);
                        possibleImagePath = fullPath + "_pri" + possibleExt;

                        if (extractAsPng)
                            img_prio.Save(_T(possibleImagePath.c_str()), Gdiplus::ImageFormatPNG);
                        else
                            Save8BitBmp(possibleImagePath, bmi_prio, pBitsDest_prio, 0);
                                                
                        BYTE *bmpBufferAlpha = NULL;
                        for (int n = 0; n < 1; n++)
                        {
                            bitmap.Attach(GetPicBitmap(PicScreen::Visual, pic, palette, pic.Size.cx, pic.Size.cy, &bmi, &pBitsDest));
                            BITMAP bmp;
                            BITMAP bmp_prio;
                            std::string possibleImagePath = fullPath + ".";
                            possibleImagePath += std::to_string(n) + possibleExt;
                            bitmap.GetBitmap(&bmp);
                            bitmap_prio.GetBitmap(&bmp_prio);
                            BYTE *bmpBuffer = (BYTE *)GlobalAlloc(GPTR,
                                                                  bmp.bmWidthBytes * bmp.bmHeight);
                            BYTE *bmpBuffer_prio = (BYTE *)GlobalAlloc(GPTR,
                                                                       bmp_prio.bmWidthBytes * bmp_prio.bmHeight);
                            BYTE *bmpBuffer_prioAlpha = (BYTE *)GlobalAlloc(GPTR,
                                                                            bmp.bmWidthBytes * bmp.bmHeight);
                            bitmap.GetBitmapBits(bmp.bmWidthBytes * bmp.bmHeight,
                                                 bmpBuffer);
                            bitmap_prio.GetBitmapBits(bmp_prio.bmWidthBytes * bmp_prio.bmHeight,
                                                      bmpBuffer_prio);
                            bitmap_prio.GetBitmapBits(bmp_prio.bmWidthBytes * bmp_prio.bmHeight,
                                                      bmpBuffer_prioAlpha);
                            bitmap.SetBitmapBits(bmp.bmWidthBytes * bmp.bmHeight,
                                                 bmpBuffer);
                            img.Attach(bitmap);


                            CImage imgout;
                            imgout.Create(pic.Size.cx, pic.Size.cy, 32, CImage::createAlphaChannel);
                            bool saveFile = false;

                            // Copy the source image to the destination image
                            HDC hdc = imgout.GetDC();
                            img.BitBlt(hdc, 0, 0);
                            imgout.ReleaseDC();
                            BYTE *pImgBits = (BYTE *)imgout.GetBits();

                            for (int x = 0; x < pic.Size.cx; ++x)
                                for (int y = 0; y < pic.Size.cy; ++y)
                                {
                                    COLORREF c1;
                                    c1 = img.GetPixel(x, y); // user image

                                    //BYTE priPixelRed = bmi_prio.bmiColors[bmpBuffer_prio[((y * pic.Size.cx) + x)]].rgbRed;
                                    //BYTE priPixelGreen = bmi_prio.bmiColors[bmpBuffer_prio[((y * pic.Size.cx) + x)]].rgbGreen;
                                    //BYTE priPixelBlue = bmi_prio.bmiColors[bmpBuffer_prio[((y * pic.Size.cx) + x)]].rgbBlue;

                                    
	                                int pitch = imgout.GetPitch();

                                    // if (bmpBuffer_prio[((y * pic.Size.cx) + x)] != n && priPixelGreen == 255)
                                    if (bmpBuffer_prio[((y * pic.Size.cx) + x)] != n)
                                    {
                                        // Calculate the pixel position in memory
                                        BYTE *pPixel = pImgBits + y * pitch + x * 4;                                      

                                        // Get the pixel color
                                        COLORREF c1 = RGB(pPixel[2], pPixel[1], pPixel[0]); // BGR order
                                       
                                        if (bmi.bmiColors[bmpBuffer[((y * pic.Size.cx) + x)]].rgbReserved != 0x0)
                                        {
                                            if (extractAsPng)
                                                pPixel[3] = 255;
                                            else
                                            {
                                                pPixel[2] = 0;
                                                pPixel[1] = 0;
                                                pPixel[0] = 0;
                                            }
                                            
                                        }
                                                                        
                                        saveFile = true;
                                    }
                                }

                            if (saveFile)
                            {
                                if (extractAsPng)
                                    imgout.Save(_T(possibleImagePath.c_str()), Gdiplus::ImageFormatPNG);
                                else
                                    imgout.Save(_T(possibleImagePath.c_str()), Gdiplus::ImageFormatBMP);
                            }
                        }
                    }
                }
                if (extractViewImages && (blob->GetType() == ResourceType::View))
				{
					count++;
					if (progress)
					{
						keepGoing = progress->SetProgress(possibleImagePath, count, totalCount);
					}

					std::unique_ptr<ResourceEntity> view = CreateResourceFromResourceData(*blob);
					std::unique_ptr<PaletteComponent> optionalPalette;
					if (view->GetComponent<RasterComponent>().Traits.PaletteType == PaletteType::VGA_256)
					{
						optionalPalette = appState->GetResourceMap().GetMergedPalette(*view, 999);
					}
					bitmap.Attach(CreateBitmapFromResource(*view, optionalPalette.get(), &bmi, &pBitsDest));

                    ExportViewResourceAsCelImages(*view, optionalPalette.get(), destinationFolder.c_str(), extractAsPng);
				}
                if (extractViewImages && (blob->GetType() == ResourceType::Font))
                {
                    filename = GetFileNameFor(*blob);
                    fullPath = destinationFolder + filename;
                    count++;
                    if (progress)
                    {
                        keepGoing = progress->SetProgress(possibleImagePath, count, totalCount);
                    }

                    std::unique_ptr<ResourceEntity> font = CreateResourceFromResourceData(*blob);
                    std::unique_ptr<PaletteComponent> optionalPalette;
                    if (font->GetComponent<RasterComponent>().Traits.PaletteType == PaletteType::VGA_256)
                    {
                        optionalPalette = appState->GetResourceMap().GetMergedPalette(*font, 999);
                    }
                    bitmap.Attach(CreateBitmapFromResource(*font, optionalPalette.get(), &bmi, &pBitsDest));
                    ExportFontResourceAsCelImages(*font, optionalPalette.get(), destinationFolder.c_str());
                }
				if ((HBITMAP)bitmap)
				{
                    //Save8BitBmp(possibleImagePath, bmi, pBitsDest, 0);
				}

				if (disassembleScripts && (blob->GetType() == ResourceType::Script))
				{
					count++;
					std::string scriptPath = fullPath + ".txt";
					if (progress)
					{
						keepGoing = progress->SetProgress(scriptPath, count, totalCount);
					}

					// Supply the heap stream here, since we want it match patch vs vs not.
					std::unique_ptr<sci::istream> heapStream;
					std::unique_ptr<ResourceBlob> heapBlob = appState->GetResourceMap().Helper().MostRecentResource(ResourceType::Heap, blob->GetNumber(), ResourceEnumFlags::ExcludePatchFiles);
					if (heapBlob)
					{
						heapStream = std::make_unique<sci::istream>(heapBlob->GetReadStream());
					}

					CompiledScript compiledScript(blob->GetNumber());
					compiledScript.Load(appState->GetResourceMap().Helper(), appState->GetVersion(), blob->GetNumber(), blob->GetReadStream(), heapStream.get());
					std::stringstream out;
					DisassembleScript(compiledScript, out, &scriptLookups, &objectFileLookups, appState->GetResourceMap().GetVocab000());
					std::string actualPath = MakeTextFile(out.str().c_str(), scriptPath.c_str());
				}

				if (extractMessages && (blob->GetType() == ResourceType::Message))
				{
					count++;
					std::string msgPath = fullPath + "-msg.txt";
					std::unique_ptr<ResourceEntity> resource = CreateResourceFromResourceData(*blob);
					ExportMessageToFile(resource->GetComponent<TextComponent>(), msgPath);
				}

				if (generateWavs && (blob->GetType() == ResourceType::Audio))
				{
					count++;
					std::string wavPath = fullPath + ".wav";
					std::unique_ptr<ResourceEntity> resource = CreateResourceFromResourceData(*blob);
					WriteWaveFile(wavPath, resource->GetComponent<AudioComponent>());
				}
			}
		}
		catch (std::exception)
		{

		}
	}

	// Finally, the sync36 and audio36 resources and the audio maps
	if (keepGoing)
	{
		auto audioMapContainer = appState->GetResourceMap().Resources(ResourceTypeFlags::AudioMap, ResourceEnumFlags::MostRecentOnly | ResourceEnumFlags::ExcludePatchFiles);
		for (auto &blob : *audioMapContainer)
		{
			if (extractResources)
			{
				count++;
				std::string filename = GetFileNameFor(*blob);
				std::string fullPath = destinationFolder + filename;
				blob->SaveToFile(fullPath);
			}

			if ((blob->GetNumber() != version.AudioMapResourceNumber) && (extractResources || generateWavs))
			{
				count++;
				auto subResourceContainer = appState->GetResourceMap().Resources(ResourceTypeFlags::Audio, ResourceEnumFlags::MostRecentOnly, blob->GetNumber());
				if (progress)
				{
					keepGoing = progress->SetProgress(fmt::format("Files for audio map {0}", blob->GetNumber()).c_str(), count, totalCount);
				}
				if (keepGoing)
				{
					for (auto &blobSubs : *subResourceContainer)
					{
						if (extractResources)
						{
							SaveAudioBlobToFiles(*blobSubs, destinationFolder);
						}

						if (generateWavs)
						{
							std::string filename = GetFileNameFor(*blobSubs);
							std::string wavPath = destinationFolder + filename + ".wav";
							std::unique_ptr<ResourceEntity> resource = CreateResourceFromResourceData(*blobSubs);
							WriteWaveFile(wavPath, resource->GetComponent<AudioComponent>());
						}
					}
				}
			}
		}
	}
}
