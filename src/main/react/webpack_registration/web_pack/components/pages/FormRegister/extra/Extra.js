import React from "react";
import Agreement from "../agreement/Agreement";
import { Link } from "react-router-dom";

const Extra = ({ whatIsPage, setChecked }) => {

  const { isChecking, setIsChecking } = setChecked;
  return (
    <div className="extra">
      <div className="checkbox">
        <input
          checked={isChecking}
          id="check"
          name="checkbox"
          type="checkbox"
          onChange={event => {
            event.target.checked ? setIsChecking(true) : setIsChecking(false);
          }}
        />
        <label className="check" htmlFor="check">
          <svg width="10" height="8" viewBox="0 0 10 8" fill="none" xmlns="http://www.w3.org/2000/svg">
            <mask id="path-1-inside-1" fill="white">
              <path fillRule="evenodd" clipRule="evenodd" d="M10.0007 0.835938L4.10237 7.00116L0.998047 3.75638"/>
            </mask>
            <path fillRule="evenodd" clipRule="evenodd" d="M10.0007 0.835938L4.10237 7.00116L0.998047 3.75638" fill="black" fillOpacity="0.01"/>
            <path d="M4.10237 7.00116L2.65722 8.38375L4.10238 9.89429L5.54752 8.38375L4.10237 7.00116ZM8.55551 -0.546645L2.65722 5.61858L5.54752 8.38375L11.4458 2.21852L8.55551 -0.546645ZM5.54751 5.61857L2.44319 2.37379L-0.447099 5.13897L2.65722 8.38375L5.54751 5.61857Z" fill="white" mask="url(#path-1-inside-1)"/>
          </svg>
        </label>
        <label htmlFor="check">
          <Agreement whatIsPage={whatIsPage} />
        </label>
      </div>
      {whatIsPage && (
        <span>
          <Link to="/registration/register">Не можете войти?</Link>
        </span>
      )}
    </div>
  );
};

export default Extra;
