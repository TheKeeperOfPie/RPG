package com.winsonchiu.rpg;

import android.app.Activity;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentRender.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentRender#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentRender extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private Activity activity;
    private Renderer renderer;
    private GLSurfaceView glSurfaceView;
    private ImageView imageDirectionControls;
    private ImageView imageInteractControl;
    private FastOutLinearInInterpolator interpolator;
    private ImageView imageInventoryControl;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentRender.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentRender newInstance(String param1, String param2) {
        FragmentRender fragment = new FragmentRender();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentRender() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        renderer = new Renderer(activity, new Renderer.EventListener() {
            @Override
            public void pickUpItem(Item item) {
                mListener.getControllerInventory().addItem(item);
            }

            @Override
            public ControllerInventory getControllerInventory() {
                return mListener.getControllerInventory();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_render, container, false);

        interpolator = new FastOutLinearInInterpolator();

        glSurfaceView = (GLSurfaceView) view.findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.setPreserveEGLContextOnPause(true);

        imageDirectionControls = (ImageView) view.findViewById(R.id.image_direction_controls);
        imageDirectionControls.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // TODO: Adjust speed based on how far stick is shifted

                int width = imageDirectionControls.getWidth();
                int height = imageDirectionControls.getHeight();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        if (event.getY() < height / 5 * 2) {
                            // Move up
                            renderer.getPlayer().setMovementY(interpolator.getInterpolation((event.getY() - height) / height * -2 - 0.8f));
                        }
                        else if (event.getY() > height / 5 * 3) {
                            // Move down
                            renderer.getPlayer().setMovementY(interpolator.getInterpolation((event.getY() - height) / height * 2 + 1.2f) * -1);
                        }
                        else {
                            renderer.getPlayer().setMovementY(0);
                        }

                        if (event.getX() < width / 5 * 2) {
                            // Move left
                            renderer.getPlayer().setMovementX(interpolator.getInterpolation((event.getX() - width) / width * -2 - 0.8f) * -1);
                        }
                        else if (event.getX() > width / 5 * 3) {
                            // Move right
                            renderer.getPlayer().setMovementX(interpolator.getInterpolation((event.getX() - width) / width * 2 + 1.2f));
                        }
                        else {
                            renderer.getPlayer().setMovementX(0);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        renderer.getPlayer().setMovementX(0);
                        renderer.getPlayer().setMovementY(0);
                        break;
                }

                return true;
            }
        });

        imageInteractControl = (ImageView) view.findViewById(R.id.image_interact_control);
        imageInteractControl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    renderer.getPlayer().startNewAttack(renderer);
                    return true;
                }
                return false;
            }
        });

        imageInventoryControl = (ImageView) view.findViewById(R.id.image_inventory_control);
        imageInventoryControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getFragmentManager().beginTransaction().replace(R.id.frame_fragment, FragmentInventory.newInstance("", "")).addToBackStack(null).commit();
                Toast.makeText(activity, "Fragment added", Toast.LENGTH_SHORT).show();
            }
        });

        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        glSurfaceView.setRenderer(renderer);
    }

    @Override
    public void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    public void onPause() {
        glSurfaceView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        renderer.release();
        super.onStop();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        activity = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        ControllerInventory getControllerInventory();
    }

}