import React , { useEffect } from 'react';

import ProductPhotoCarousel from './ProductPhotoCarousel/ProductPhotoCarousel';
import ProductDescription from './ProductDescription/ProductDescription';
import ProductOwnerData from './ProductOwnerData/ProductOwnerData';
import ProductPostData from './ProductPostData/ProductPostData';
import ProductOffers from './ProductOffers/ProductOffers';
import TitleBigBlue from '../../components/title_Big_Blue';
import { getProduct } from '../../REST/Resources/fetchProfile';

import './ProductPage.scss';

const ProductPage = () => {
	
	useEffect(() => {
		getProduct(46).then(({data})=>{
			console.log(data);
		})
	}, [])
	
	return (
		<div>
			<section className = 'topSection'>
				<div className = 'productPageContainer'>
					<div className = 'breadСrumbs'>Категории / Детские вещи / Унисекс / <span>Кофта детская с кроликом</span>
					</div>
					<div className = 'productPageInner'>
						<div className = 'carouselAndDescription'>
							<ProductPhotoCarousel/>
							<ProductDescription/>
						</div>
						<div className = 'ownerAndPost'>
							<ProductOwnerData/>
							<ProductPostData/>
						</div>
					</div>
				</div>
			</section>
			<section>
				<div className = 'productPageContainer'>
					<div className = 'productPageInner'>
						<div className = 'sectionHeading'>
							<TitleBigBlue text = { 'Вас так же могут заинтересовать' }/>
						</div>
						<ProductOffers/>
					</div>
				</div>
			</section>
		</div>
	);
};

export default ProductPage;