import React, { useContext } from 'react';
import { useSelector } from 'react-redux';

import { ModalContext } from 'components/common/pop-up';
import { getTranslatedText } from 'components/local/localisation';

import { convertToMB } from './helper';
import { ImagePhoto } from './image-photo';
import { AddFileInput } from './add-file-input';

const PhotoFiles = ({
  imageFiles,
  preViewImage,
  setImageFiles,
  setPreViewImage,
  currentIndexImage,
  setCurrentIndexImage,
}) => {
  const { lang } = useSelector((state) => state.auth);
  const { openModal } = useContext(ModalContext);

  const filesAddHandler = (event, dropFiles = null) => {
    event.preventDefault();

    const files = Array.from(dropFiles || event.target.files);

    files.forEach((file, index, iterableArray) => {
      const notAbilityToDownload =
        10 - imageFiles.length - iterableArray.length < 0;

      const foundSameFile = imageFiles.filter(
        (image) => image.name === file.name
      );

      if (foundSameFile.length) {
        openModal({
          title: getTranslatedText('popup.errorTitle', lang),
          children: (
            <p style={{ textAlign: 'center' }}>
              {getTranslatedText('popup.addedFile', lang)}
            </p>
          ),
        });
        return;
      }

      if (!file.type.match('image') || file.type.match('image/svg')) {
        openModal({
          title: getTranslatedText('popup.errorTitle', lang),
          children: (
            <p style={{ textAlign: 'center' }}>
              {getTranslatedText('popup.pictureSelection', lang)} ( jpg, jpeg,
              png, gif ).
            </p>
          ),
        });
        return;
      }

      const { value, valueString } = convertToMB(file.size);
      if (value >= 10 && valueString.includes('MB')) {
        openModal({
          title: getTranslatedText('popup.errorTitle', lang),
          children: (
            <p style={{ textAlign: 'center' }}>
              {getTranslatedText('popup.sizeFile', lang)} {valueString}
              <br /> {getTranslatedText('popup.selectFile', lang)}
            </p>
          ),
        });
        return;
      }
      if (notAbilityToDownload) {
        openModal({
          title: getTranslatedText('popup.errorTitle', lang),
          children: (
            <p style={{ textAlign: 'center' }}>
              {getTranslatedText('popup.noSaveMore', lang)}
            </p>
          ),
        });
        return;
      }

      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = (event) => {
        if (event.target.readyState === 2) {
          setPreViewImage((prev) => [...prev, event.target.result]);
          setImageFiles((prev) => [...prev, file]);
        }
      };
    });
  };

  const removeImage = (event, index) => {
    event.preventDefault();

    const newImageFiles = [...imageFiles];
    const newPreViewImage = [...preViewImage];
    newPreViewImage.splice(index, 1);
    newImageFiles.splice(index, 1);
    setImageFiles(newImageFiles);
    setPreViewImage(newPreViewImage);
  };

  const dragStartHandler = (e, index) => {
    setCurrentIndexImage(index);
  };

  const dragEndHandler = (e) => {
    e.target.style.background = 'white';
  };

  const dragOverHandler = (e) => {
    e.preventDefault();
    e.target.style.background = 'lightgrey';
  };

  const changeStateForImagesWhenDrop = (
    index,
    processedArray,
    setProcessedArray
  ) => {
    const newPrevArr = [...processedArray];
    const underPrevImage = newPrevArr[index];
    const currentPrevImage = newPrevArr[currentIndexImage];
    newPrevArr[currentIndexImage] = underPrevImage;
    newPrevArr[index] = currentPrevImage;
    setProcessedArray(newPrevArr);
  };

  const dropHandler = (e, index) => {
    e.preventDefault();
    e.target.style.background = 'white';
    changeStateForImagesWhenDrop(index, preViewImage, setPreViewImage);
    changeStateForImagesWhenDrop(index, imageFiles, setImageFiles);
  };
  return (
    <div className="files">
      <h3>{getTranslatedText(`addAdv.uploadDescription`, lang)}</h3>
      <p>{getTranslatedText(`addAdv.firstUploadDescription`, lang)}</p>
      <p>
        {getTranslatedText(`addAdv.photosUploaded`, lang)} {imageFiles.length}{' '}
        {getTranslatedText(`addAdv.from`, lang)} 10
      </p>
      <div className="files_wrapper">
        {preViewImage.map((url, index) => (
          <ImagePhoto
            url={url}
            key={index}
            index={index}
            removeImage={removeImage}
            onDragEnd={(e) => dragEndHandler(e)}
            onDrop={(e) => dropHandler(e, index)}
            onDragLeave={(e) => dragEndHandler(e)}
            onDragOver={(e) => dragOverHandler(e)}
            onDragStart={(e) => dragStartHandler(e, index)}
          />
        ))}

        {imageFiles.length < 10 && <AddFileInput onChange={filesAddHandler} />}
      </div>
    </div>
  );
};
export { PhotoFiles };